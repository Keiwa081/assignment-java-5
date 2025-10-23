package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import poly.edu.model.Cart;
import poly.edu.model.Order;
import poly.edu.model.OrderDetail;
import poly.edu.model.Product;
import poly.edu.repository.CartRepository;
import poly.edu.repository.OrderRepository;
import poly.edu.repository.OrderDetailRepository;
import poly.edu.repository.ProductRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Get all orders for a specific account (for customer view)
     */
    public List<Order> getOrdersByAccount(Integer accountId) {
        if (accountId == null) {
            return orderRepository.findAllByOrderByOrderDateDesc();
        }
        return orderRepository.findByAccountIdOrderByOrderDateDesc(accountId);
    }

    /**
     * Get all orders (for admin)
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    /**
     * Get order by ID
     */
    public Order getOrderById(Integer orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    /**
     * Get order details
     */
    public List<OrderDetail> getOrderDetails(Integer orderId) {
        return orderDetailRepository.findByOrderId(orderId);
    }

    /**
     * Create order from cart
     * ✅ FIX: Sử dụng giá đã giảm (discounted price) thay vì giá gốc
     */
    @Transactional
    public String createOrderFromCart(Integer accountId, String shippingAddress, String phone, String note) {
        try {
            // 1. Get cart items
            List<Cart> cartItems = cartRepository.findByAccountId(accountId);
            
            if (cartItems.isEmpty()) {
                return "empty_cart";
            }

            // 2. Validate stock and calculate total
            double totalAmount = 0.0;
            List<OrderDetail> orderDetails = new ArrayList<>();

            for (Cart cartItem : cartItems) {
                Optional<Product> productOpt = productRepository.findById(cartItem.getProductId());
                
                if (productOpt.isEmpty()) {
                    return "product_not_found:" + cartItem.getProductId();
                }

                Product product = productOpt.get();

                // Check stock availability
                if (product.getQuantity() <= 0) {
                    return "out_of_stock:" + product.getName();
                }

                if (product.getQuantity() < cartItem.getQuantity()) {
                    return "insufficient_stock:" + product.getName() + ":" + product.getQuantity();
                }

                // ✅ FIX: Sử dụng giá đã giảm thay vì giá gốc
                Double finalPrice = product.getDiscountedPrice(); // Thay vì product.getPrice()
                double itemTotal = finalPrice * cartItem.getQuantity();
                totalAmount += itemTotal;

                OrderDetail detail = OrderDetail.builder()
                        .productId(product.getProductId())
                        .quantity(cartItem.getQuantity())
                        .unitPrice(finalPrice) // ✅ Lưu giá đã giảm
                        .build();
                
                orderDetails.add(detail);
            }

            // 3. Create order with default statusId = 1 (Pending)
            Order order = Order.builder()
                    .accountId(accountId)
                    .orderDate(LocalDateTime.now())
                    .statusId(1)
                    .total(totalAmount)
                    .shippingAddress(shippingAddress)
                    .phone(phone)
                    .note(note)
                    .build();

            order = orderRepository.save(order);

            // 4. Create order details and update product quantities
            for (int i = 0; i < orderDetails.size(); i++) {
                OrderDetail detail = orderDetails.get(i);
                detail.setOrderId(order.getOrderId());
                orderDetailRepository.save(detail);

                // Update product quantity
                Cart cartItem = cartItems.get(i);
                Product product = productRepository.findById(cartItem.getProductId()).get();
                product.setQuantity(product.getQuantity() - cartItem.getQuantity());
                productRepository.save(product);
            }

            // 5. Clear cart
            cartRepository.deleteByAccountId(accountId);

            return "success:" + order.getOrderId();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("ERROR in createOrderFromCart: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
            }
            throw new RuntimeException("Failed to create order: " + e.getMessage(), e);
        }
    }

    /**
     * Cancel order (by customer - only if status = 1)
     */
    @Transactional
    public boolean cancelOrder(Integer orderId, Integer accountId) {
        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            
            if (orderOpt.isEmpty()) {
                return false;
            }

            Order order = orderOpt.get();

            if (!order.getAccountId().equals(accountId)) {
                return false;
            }

            if (!Integer.valueOf(1).equals(order.getStatusId())) {
                return false;
            }

            order.setStatusId(5);
            orderRepository.save(order);

            List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(orderId);
            for (OrderDetail detail : orderDetails) {
                Product product = productRepository.findById(detail.getProductId()).orElse(null);
                if (product != null) {
                    product.setQuantity(product.getQuantity() + detail.getQuantity());
                    productRepository.save(product);
                }
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update order status (by admin)
     */
    @Transactional
    public boolean updateOrderStatus(Integer orderId, Integer statusId) {
        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            
            if (orderOpt.isEmpty()) {
                return false;
            }

            Order order = orderOpt.get();
            Integer oldStatus = order.getStatusId();
            
            if (statusId == 5 && oldStatus != 5) {
                List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(orderId);
                for (OrderDetail detail : orderDetails) {
                    Product product = productRepository.findById(detail.getProductId()).orElse(null);
                    if (product != null) {
                        product.setQuantity(product.getQuantity() + detail.getQuantity());
                        productRepository.save(product);
                    }
                }
            }
            
            order.setStatusId(statusId);
            orderRepository.save(order);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public long getTotalOrdersCount(Integer accountId) {
        return orderRepository.countByAccountId(accountId);
    }

    public Double getTotalAmountSpent(Integer accountId) {
        return orderRepository.getTotalAmountByAccountId(accountId);
    }

    public List<Order> getOrdersByStatusId(Integer statusId) {
        return orderRepository.findByStatusIdOrderByOrderDateDesc(statusId);
    }

    public List<Order> getOrdersByAccountAndStatusId(Integer accountId, Integer statusId) {
        return orderRepository.findByAccountIdAndStatusIdOrderByOrderDateDesc(accountId, statusId);
    }
    
    public long countOrdersByStatus(Integer statusId) {
        return orderRepository.countByStatusId(statusId);
    }
    
    public Double getTotalRevenue() {
        return orderRepository.getTotalRevenue();
    }
    
    public List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findOrdersByDateRange(startDate, endDate);
    }
}
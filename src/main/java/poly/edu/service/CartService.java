package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.model.Cart;
import poly.edu.repository.CartRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductService productService;

    // Add product to cart
    public String addToCart(Integer accountId, Integer productId, Integer quantity) {
        // Check if product is in stock
        if (!productService.isInStock(productId, quantity)) {
            Integer availableQuantity = productService.getAvailableQuantity(productId);
            if (availableQuantity == 0) {
                return "out_of_stock";
            } else {
                return "insufficient_stock:" + availableQuantity;
            }
        }

        // Check if item already exists in cart
        Optional<Cart> existingCartItem = cartRepository.findByAccountIdAndProductId(accountId, productId);
        
        if (existingCartItem.isPresent()) {
            // Update quantity
            Cart cartItem = existingCartItem.get();
            Integer newQuantity = cartItem.getQuantity() + quantity;
            
            // Check if total quantity exceeds available stock
            if (!productService.isInStock(productId, newQuantity)) {
                Integer availableQuantity = productService.getAvailableQuantity(productId);
                return "insufficient_stock:" + availableQuantity;
            }
            
            cartItem.setQuantity(newQuantity);
            cartRepository.save(cartItem);
            
            // Reduce product quantity
            productService.reduceProductQuantity(productId, quantity);
            
            return "updated:" + newQuantity;
        } else {
            // Add new item to cart
            Cart cartItem = Cart.builder()
                    .accountId(accountId)
                    .productId(productId)
                    .quantity(quantity)
                    .addedAt(LocalDateTime.now())
                    .build();
            
            cartRepository.save(cartItem);
            
            // Reduce product quantity
            productService.reduceProductQuantity(productId, quantity);
            
            return "added:" + quantity;
        }
    }

    // Get cart items for account
    public List<Cart> getCartItems(Integer accountId) {
        return cartRepository.findByAccountId(accountId);
    }

    // Update cart item quantity
    public String updateCartItemQuantity(Integer accountId, Integer productId, Integer newQuantity) {
        Optional<Cart> cartItemOpt = cartRepository.findByAccountIdAndProductId(accountId, productId);
        
        if (cartItemOpt.isPresent()) {
            Cart cartItem = cartItemOpt.get();
            Integer oldQuantity = cartItem.getQuantity();
            
            if (newQuantity <= 0) {
                // Remove item from cart
                removeFromCart(accountId, productId);
                return "removed";
            }
            
            // Check if new quantity is available
            Integer quantityDifference = newQuantity - oldQuantity;
            Integer availableQuantity = productService.getAvailableQuantity(productId);
            
            if (quantityDifference > availableQuantity) {
                return "insufficient_stock:" + availableQuantity;
            }
            
            // Update cart item
            cartItem.setQuantity(newQuantity);
            cartRepository.save(cartItem);
            
            // Adjust product quantity
            if (quantityDifference > 0) {
                productService.reduceProductQuantity(productId, quantityDifference);
            } else if (quantityDifference < 0) {
                productService.restoreProductQuantity(productId, Math.abs(quantityDifference));
            }
            
            return "updated:" + newQuantity;
        }
        
        return "not_found";
    }

    // Remove item from cart
    public boolean removeFromCart(Integer accountId, Integer productId) {
        Optional<Cart> cartItemOpt = cartRepository.findByAccountIdAndProductId(accountId, productId);
        
        if (cartItemOpt.isPresent()) {
            Cart cartItem = cartItemOpt.get();
            
            // Restore product quantity
            productService.restoreProductQuantity(productId, cartItem.getQuantity());
            
            // Remove from cart
            cartRepository.delete(cartItem);
            return true;
        }
        
        return false;
    }

    // Clear entire cart
    public boolean clearCart(Integer accountId) {
        List<Cart> cartItems = cartRepository.findByAccountId(accountId);
        
        // Restore all product quantities
        for (Cart cartItem : cartItems) {
            productService.restoreProductQuantity(cartItem.getProductId(), cartItem.getQuantity());
        }
        
        // Clear cart
        cartRepository.deleteByAccountId(accountId);
        return true;
    }

    // Get cart item count
    public long getCartItemCount(Integer accountId) {
        return cartRepository.countByAccountId(accountId);
    }

    // Get total quantity in cart
    public Integer getTotalQuantity(Integer accountId) {
        return cartRepository.getTotalQuantityByAccountId(accountId);
    }

    // Check if product is in cart
    public boolean isProductInCart(Integer accountId, Integer productId) {
        return cartRepository.existsByAccountIdAndProductId(accountId, productId);
    }

    // Get cart item by account and product
    public Optional<Cart> getCartItem(Integer accountId, Integer productId) {
        return cartRepository.findByAccountIdAndProductId(accountId, productId);
    }
}
package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.model.Product;
import poly.edu.repository.ProductRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // Get all products with pagination
    public Page<Product> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findAll(pageable);
    }

    // Get featured products (high rating and available)
    public Page<Product> getFeaturedProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findFeaturedProducts(pageable);
    }

    // Get products by category with pagination
    public Page<Product> getProductsByCategory(Integer categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findAvailableProductsByCategory(categoryId, pageable);
    }

    // Get product by ID
    public Optional<Product> getProductById(Integer productId) {
        return productRepository.findById(productId);
    }

    // Search products
    public Page<Product> searchProducts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.searchProducts(keyword, pageable);
    }

    // Get available products only
    public Page<Product> getAvailableProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByQuantityGreaterThan(0, pageable);
    }

    // Check if product is in stock
    public boolean isInStock(Integer productId, Integer requestedQuantity) {
        Optional<Product> product = productRepository.findById(productId);
        return product.isPresent() && product.get().getQuantity() >= requestedQuantity;
    }

    // Get available quantity for a product
    public Integer getAvailableQuantity(Integer productId) {
        Optional<Product> product = productRepository.findById(productId);
        return product.map(Product::getQuantity).orElse(0);
    }

    // Reduce product quantity (for cart operations)
    public boolean reduceProductQuantity(Integer productId, Integer quantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            if (product.getQuantity() >= quantity) {
                product.setQuantity(product.getQuantity() - quantity);
                productRepository.save(product);
                return true;
            }
        }
        return false;
    }

    // Restore product quantity (when removing from cart)
    public boolean restoreProductQuantity(Integer productId, Integer quantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setQuantity(product.getQuantity() + quantity);
            productRepository.save(product);
            return true;
        }
        return false;
    }

    // Get out of stock products
    public List<Product> getOutOfStockProducts() {
        return productRepository.findByQuantityLessThanEqual(0);
    }

    // Get products by price range
    public Page<Product> getProductsByPriceRange(Double minPrice, Double maxPrice, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByPriceBetween(minPrice, maxPrice, pageable);
    }

    // Get products by rating
    public Page<Product> getProductsByRating(Double minRating, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByRatingGreaterThanEqual(minRating, pageable);
    }
}




package com.project.code.Service;

import com.project.code.Model.*;
import com.project.code.Repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    public void saveOrder(PlaceOrderRequestDTO placeOrderRequest) {
        // Retrieve or Create the Customer
        Customer customer = customerRepository.findByEmail(placeOrderRequest.getCustomerEmail());
        if (customer == null) {
            customer = new Customer();
            customer.setName(placeOrderRequest.getCustomerName());
            customer.setEmail(placeOrderRequest.getCustomerEmail());
            customer.setPhone(placeOrderRequest.getCustomerPhone());
            customer = customerRepository.save(customer);
        }

        // Retrieve the Store
        Optional<Store> storeOpt = storeRepository.findById(placeOrderRequest.getStoreId());
        if (!storeOpt.isPresent()) {
            throw new RuntimeException("Store not found");
        }
        Store store = storeOpt.get();

        // Create OrderDetails
        OrderDetails orderDetails = new OrderDetails(customer, store, placeOrderRequest.getTotalPrice(), LocalDateTime.now());
        orderDetails = orderDetailsRepository.save(orderDetails);

        // Create and Save OrderItems
        for (PurchaseProductDTO purchase : placeOrderRequest.getPurchaseProduct()) {
            Inventory inventory = inventoryRepository.findByProductIdandStoreId(purchase.getId(), placeOrderRequest.getStoreId());
            if (inventory != null) {
                inventory.setStockLevel(inventory.getStockLevel() - purchase.getQuantity());
                inventoryRepository.save(inventory);
            }

            Product product = productRepository.findById(purchase.getId()).orElse(null);
            if (product != null) {
                OrderItem orderItem = new OrderItem(orderDetails, product, purchase.getQuantity(), purchase.getPrice());
                orderItemRepository.save(orderItem);
            }
        }
    }
}

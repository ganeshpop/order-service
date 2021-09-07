package com.order.service;


import com.order.bean.InventoryItem;
import com.order.bean.OrderItem;
import com.order.bean.Product;
import com.order.bean.UserOrder;
import com.order.exception.InvalidProductQuantityException;
import com.order.exception.ProductNotFoundException;
import com.order.exception.ProductNotFoundInInventoryException;
import com.order.persistence.OrderDaoInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class OrderService implements OrderServiceInterface {
    private RestTemplate restTemplate;

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private OrderDaoInterface orderDao;

    @Autowired
    public void setOrderDao(OrderDaoInterface orderDao) {
        this.orderDao = orderDao;
    }

    @Override
    public Optional<UserOrder> findOrderById(Long id) {
        return orderDao.findById(id);
    }

    @Override
    public List<UserOrder> getAllUserOrders() {
        return orderDao.findAll();
    }

    @Override
    public UserOrder getLastUserOrderByUserName(String userName) {
         List<UserOrder> userOrderList =  orderDao.getUserOrderByUserNameOrderByIdDesc(userName);
         if(!userOrderList.isEmpty()){
             return userOrderList.get(0);
         }
         return null;
    }

    @Override
    public List<UserOrder> getUserOrdersByUserName(String userName) {
        return orderDao.getUserOrdersByUserName(userName);
    }

    @Override
    public UserOrder createOrder(UserOrder userOrder) {
        List<OrderItem> items = userOrder.getItems();
        List<OrderItem> validItems = new ArrayList<>();
        int itemCount = 0;

        for (OrderItem orderItem : items) {
            InventoryItem inventoryItem = restTemplate.getForObject("http://localhost:8082/inventories/order/code/" + orderItem.getProductCode(), InventoryItem.class);
            if (inventoryItem != null) {
                if (orderItem.getQuantity() <= inventoryItem.getAvailableQuantity()) {
                    Product product = restTemplate.getForObject("http://localhost:8084/products/order/code/" + orderItem.getProductCode(), Product.class);
                    if (product != null) {
                        orderItem.setProductPrice(BigDecimal.valueOf(product.getPrice() * orderItem.getQuantity()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                        userOrder.setTotalFare(userOrder.getTotalFare() + orderItem.getProductPrice());
                        validItems.add(orderItem);
                        itemCount += orderItem.getQuantity();
                    } else {
                        throw new ProductNotFoundException("Product With Product Code " + orderItem.getProductCode() + " Not Found");
                    }
                } else {
                    throw new InvalidProductQuantityException("Requested Quantity [" + orderItem.getQuantity() + "] of Product " + orderItem.getProductCode() + " is More Than Available Quantity [" + inventoryItem.getAvailableQuantity() + "]");
                }
            } else {
                throw new ProductNotFoundInInventoryException("Product With Product Code " + orderItem.getProductCode() + " Not Found In Inventory ");
            }
        }
        userOrder.setItems(validItems);
        userOrder.setItemCount(itemCount);
        return orderDao.save(userOrder);
    }
}
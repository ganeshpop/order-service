package com.order.resources;


import com.order.bean.UserOrderList;
import com.order.bean.UserOrder;
import com.order.exception.UserNotFoundException;
import com.order.service.OrderServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;


@RestController
@CrossOrigin
@RequestMapping(path = "/orders")
public class OrderResource {


    private OrderServiceInterface orderService;


    @Autowired
    public void setOrderService(OrderServiceInterface orderService) {
        this.orderService = orderService;
    }

    @GetMapping(produces = "Application/json")
    UserOrderList getAllUserOrders() {
        UserOrderList userOrderList = new UserOrderList();
        userOrderList.setOrders(orderService.getAllUserOrders());
        return userOrderList;
    }

    @PostMapping(produces = "Application/json", consumes = "Application/json")
    ResponseEntity<Object> saveUserOrder(@Valid @RequestBody UserOrder userOrder) {
        UserOrder createdUserOrder = orderService.createOrder(userOrder);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(createdUserOrder.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping(path = "/{id}", produces = "Application/json")
    UserOrder getUserOrderById(@PathVariable("id") Long id) throws UserNotFoundException {
        Optional<UserOrder> userOrder = orderService.findOrderById(id);
        if (!userOrder.isPresent()) {
            throw new UserNotFoundException("No Order With ID " + id + " Found");
        }
        return userOrder.get();
    }

}
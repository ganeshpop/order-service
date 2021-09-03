package com.order.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class UserOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String customerEmail;
    private String customerAddress;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items;


    public UserOrder(String customerEmail, String customerAddress, List<OrderItem> items){
        this.customerEmail = customerEmail;
        this.customerAddress = customerAddress;
        this.items = items;
    }
}

package com.sist.baemin.order.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@Log4j2
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    @GetMapping("/form")
    public String orderPage(
            @RequestParam("deliveryfee") Long deliveryFee,
            @RequestParam("price") Long price,
            @RequestParam(value = "discount", required = false, defaultValue = "0") Long discount,
            Model model
    ) {
        long paymentAmount = price + deliveryFee - discount;

        model.addAttribute("price", price);
        model.addAttribute("deliveryFee", deliveryFee);
        model.addAttribute("discount", discount);
        model.addAttribute("paymentAmount", paymentAmount);

        return "order/form";
    }
}

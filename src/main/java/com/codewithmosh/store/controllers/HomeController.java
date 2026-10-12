//member 1
package com.codewithmosh.store.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class HomeController {
    @GetMapping("/") public String home() { return "redirect:/ui/products"; }
    @GetMapping("/ui/categories") public String categories() { return "categories"; }
    @GetMapping("/ui/products") public String products() { return "products"; }
    @GetMapping("/ui/customers") public String customers() { return "customers"; }
    @GetMapping("/ui/cart") public String cart() { return "cart"; }
}

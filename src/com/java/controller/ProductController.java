package com.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.java.dto.Product;
import com.java.repository.ProductRepository;

@Controller
public class ProductController {
	
	@Autowired ProductRepository rep;

	@PostMapping("/addProduct")
	public String addProduct(@ModelAttribute Product product) {
		rep.addProduct(product);
		return "redirect:/viewProducts";
	}
	
	@GetMapping("/viewProducts")
	public String viewProducts(Model model) {
		model.addAttribute("products", rep.getAllProducts());
		return "viewProducts";
	}
}

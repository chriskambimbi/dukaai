#!/usr/bin/env python3
"""
Generate JSONL training dataset for FunctionGemma fine-tuning on DukaAI.
Creates 2000 realistic examples of Zambian retail operations.
"""

import json
import random
from datetime import datetime
from typing import List, Dict, Any

# Zambian product names and categories
PRODUCTS = {
    "beverages": ["Coca-Cola", "Fanta", "Sprite", "Mosi Lager", "Castle Lite", "Maheu", "Freezit", "Minute Maid"],
    "groceries": ["Mealie Meal", "Rice", "Sugar", "Salt", "Cooking Oil", "Flour", "Beans", "Kapenta"],
    "snacks": ["Jiggies", "Munchies", "Biscuits", "Peanuts", "Popcorn", "Sweets", "Chibuku"],
    "household": ["Surf", "Omo", "Jik", "Vim", "Matches", "Candles", "Mosquito Coils", "Vaseline"],
    "bread": ["Bread", "Scones", "Doughnuts", "Buns"],
    "dairy": ["Milk", "Yoghurt", "Butter", "Cheese", "Margarine"]
}

ALL_PRODUCTS = [p for cat in PRODUCTS.values() for p in cat]

# Zambian customer names
CUSTOMER_NAMES = [
    "John", "Mary", "James", "Grace", "Peter", "Sarah", "David", "Ruth",
    "Moses", "Esther", "Joseph", "Faith", "Daniel", "Mercy", "Samuel", "Joyce",
    "Emmanuel", "Prisca", "Michael", "Angela", "Christopher", "Precious", "Benjamin", "Chimwemwe",
    "Mwamba", "Chanda", "Banda", "Tembo", "Mulenga", "Mutale", "Kunda", "Phiri"
]

# Tool declarations for DukaAI
DUKAAI_TOOLS = [
    {
        "function": {
            "name": "add_product",
            "description": "Add a new product to the inventory.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "name": {"type": "STRING", "description": "The product name"},
                    "selling_price": {"type": "NUMBER", "description": "The selling price in Kwacha"},
                    "category": {"type": "STRING", "description": "Product category"},
                    "initial_stock": {"type": "INTEGER", "description": "Initial stock quantity"},
                    "buying_price": {"type": "NUMBER", "description": "The buying/cost price in Kwacha"},
                    "barcode": {"type": "STRING", "description": "Product barcode"},
                    "low_stock_threshold": {"type": "INTEGER", "description": "Minimum stock level before alert"},
                    "unit": {"type": "STRING", "description": "Unit of measurement"}
                },
                "required": ["name", "selling_price"]
            }
        }
    },
    {
        "function": {
            "name": "edit_product",
            "description": "Edit an existing product's details.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "product_name": {"type": "STRING", "description": "The name of the product to edit"},
                    "new_name": {"type": "STRING", "description": "New name for the product"},
                    "new_selling_price": {"type": "NUMBER", "description": "New selling price in Kwacha"},
                    "new_buying_price": {"type": "NUMBER", "description": "New buying price in Kwacha"},
                    "new_category": {"type": "STRING", "description": "New category for the product"},
                    "new_low_stock_threshold": {"type": "INTEGER", "description": "New low stock alert threshold"}
                },
                "required": ["product_name"]
            }
        }
    },
    {
        "function": {
            "name": "delete_product",
            "description": "Delete a product from the inventory.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "product_name": {"type": "STRING", "description": "The name of the product to delete"},
                    "confirm": {"type": "BOOLEAN", "description": "Confirmation flag"}
                },
                "required": ["product_name"]
            }
        }
    },
    {
        "function": {
            "name": "get_product_details",
            "description": "Get detailed information about a specific product.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "product_name": {"type": "STRING", "description": "The name of the product"}
                },
                "required": ["product_name"]
            }
        }
    },
    {
        "function": {
            "name": "check_stock",
            "description": "Check the current stock level for a product.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "product_name": {"type": "STRING", "description": "The name of the product to check"}
                },
                "required": ["product_name"]
            }
        }
    },
    {
        "function": {
            "name": "update_stock",
            "description": "Update the stock quantity for an existing product.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "product_name": {"type": "STRING", "description": "The name of the product to update"},
                    "quantity": {"type": "INTEGER", "description": "The quantity to add or remove"},
                    "reason": {"type": "STRING", "description": "Reason for stock adjustment"}
                },
                "required": ["product_name", "quantity"]
            }
        }
    },
    {
        "function": {
            "name": "search_products",
            "description": "Search for products by name, category, or barcode.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "query": {"type": "STRING", "description": "Search query"},
                    "search_type": {"type": "STRING", "description": "Type of search to perform"}
                },
                "required": ["query"]
            }
        }
    },
    {
        "function": {
            "name": "get_low_stock_alerts",
            "description": "Get a list of products that are running low on stock.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "threshold": {"type": "INTEGER", "description": "Custom threshold to check"}
                },
                "required": []
            }
        }
    },
    {
        "function": {
            "name": "list_products",
            "description": "List all products in inventory.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "category": {"type": "STRING", "description": "Filter by category"},
                    "sort_by": {"type": "STRING", "description": "Field to sort by"},
                    "sort_order": {"type": "STRING", "description": "Sort order"},
                    "limit": {"type": "INTEGER", "description": "Maximum number of products to return"}
                },
                "required": []
            }
        }
    },
    {
        "function": {
            "name": "get_out_of_stock",
            "description": "Get all products that are completely out of stock.",
            "parameters": {
                "type": "OBJECT",
                "properties": {},
                "required": []
            }
        }
    },
    {
        "function": {
            "name": "get_top_selling_products",
            "description": "Get the top selling products by quantity or revenue.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "period": {"type": "STRING", "description": "Time period to analyze"},
                    "metric": {"type": "STRING", "description": "Metric to rank by"},
                    "limit": {"type": "INTEGER", "description": "Number of top products to return"}
                },
                "required": ["period"]
            }
        }
    },
    {
        "function": {
            "name": "record_sale",
            "description": "Record a sale transaction for a product.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "product_name": {"type": "STRING", "description": "The name of the product being sold"},
                    "quantity": {"type": "INTEGER", "description": "The number of units sold"},
                    "customer_name": {"type": "STRING", "description": "Optional name of the customer"},
                    "sale_type": {"type": "STRING", "description": "Type of sale: cash or credit"},
                    "unit_price": {"type": "NUMBER", "description": "Override unit price"},
                    "discount": {"type": "NUMBER", "description": "Discount amount in Kwacha"}
                },
                "required": ["product_name", "quantity"]
            }
        }
    },
    {
        "function": {
            "name": "record_batch_sale",
            "description": "Record multiple product sales in a single transaction.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "items": {"type": "STRING", "description": "Comma-separated list of items"},
                    "customer_name": {"type": "STRING", "description": "Optional customer name"},
                    "sale_type": {"type": "STRING", "description": "Type of sale"}
                },
                "required": ["items"]
            }
        }
    },
    {
        "function": {
            "name": "get_sale_history",
            "description": "Get sales history.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "period": {"type": "STRING", "description": "Time period"},
                    "product_name": {"type": "STRING", "description": "Filter by product"},
                    "customer_name": {"type": "STRING", "description": "Filter by customer"},
                    "sale_type": {"type": "STRING", "description": "Filter by sale type"}
                },
                "required": []
            }
        }
    },
    {
        "function": {
            "name": "get_today_sales",
            "description": "Get today's sales summary.",
            "parameters": {
                "type": "OBJECT",
                "properties": {},
                "required": []
            }
        }
    },
    {
        "function": {
            "name": "record_credit_sale",
            "description": "Record credit sale (pa ng'ong'ole).",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "customer_name": {"type": "STRING", "description": "Customer name"},
                    "product_name": {"type": "STRING", "description": "Product name"},
                    "quantity": {"type": "INTEGER", "description": "Quantity"},
                    "due_date": {"type": "STRING", "description": "Payment due date"}
                },
                "required": ["customer_name", "product_name", "quantity"]
            }
        }
    },
    {
        "function": {
            "name": "get_customer_balance",
            "description": "Get customer's outstanding balance.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "customer_name": {"type": "STRING", "description": "Customer name"}
                },
                "required": ["customer_name"]
            }
        }
    },
    {
        "function": {
            "name": "get_all_credit_balances",
            "description": "Get all outstanding balances.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "sort_by": {"type": "STRING", "description": "Sort by field"}
                },
                "required": []
            }
        }
    },
    {
        "function": {
            "name": "get_overdue_credits",
            "description": "Get past-due accounts.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "days_overdue": {"type": "INTEGER", "description": "Minimum days overdue"}
                },
                "required": []
            }
        }
    },
    {
        "function": {
            "name": "add_customer",
            "description": "Add new customer.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "name": {"type": "STRING", "description": "Customer name"},
                    "phone": {"type": "STRING", "description": "Phone number"},
                    "address": {"type": "STRING", "description": "Address"},
                    "notes": {"type": "STRING", "description": "Notes"}
                },
                "required": ["name"]
            }
        }
    },
    {
        "function": {
            "name": "update_customer",
            "description": "Update customer details.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "customer_name": {"type": "STRING", "description": "Customer name"},
                    "new_name": {"type": "STRING", "description": "New name"},
                    "new_phone": {"type": "STRING", "description": "New phone"},
                    "new_address": {"type": "STRING", "description": "New address"},
                    "new_notes": {"type": "STRING", "description": "New notes"}
                },
                "required": ["customer_name"]
            }
        }
    },
    {
        "function": {
            "name": "get_customer_details",
            "description": "Get customer information.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "customer_name": {"type": "STRING", "description": "Customer name"}
                },
                "required": ["customer_name"]
            }
        }
    },
    {
        "function": {
            "name": "search_customers",
            "description": "Search customers by name/phone.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "query": {"type": "STRING", "description": "Search query"}
                },
                "required": ["query"]
            }
        }
    },
    {
        "function": {
            "name": "record_payment",
            "description": "Record customer payment.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "customer_name": {"type": "STRING", "description": "Customer name"},
                    "amount": {"type": "NUMBER", "description": "Payment amount"},
                    "payment_method": {"type": "STRING", "description": "Payment method"},
                    "notes": {"type": "STRING", "description": "Payment notes"}
                },
                "required": ["customer_name", "amount"]
            }
        }
    },
    {
        "function": {
            "name": "get_payment_history",
            "description": "Get customer's payment history.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "customer_name": {"type": "STRING", "description": "Customer name"},
                    "period": {"type": "STRING", "description": "Time period"}
                },
                "required": ["customer_name"]
            }
        }
    },
    {
        "function": {
            "name": "get_today_payments",
            "description": "Get today's payments.",
            "parameters": {
                "type": "OBJECT",
                "properties": {},
                "required": []
            }
        }
    },
    {
        "function": {
            "name": "get_sales_analytics",
            "description": "Get sales analytics.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "period": {"type": "STRING", "description": "Time period"},
                    "group_by": {"type": "STRING", "description": "Group by field"}
                },
                "required": ["period"]
            }
        }
    },
    {
        "function": {
            "name": "get_revenue_summary",
            "description": "Get revenue summary.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "period": {"type": "STRING", "description": "Time period"}
                },
                "required": ["period"]
            }
        }
    },
    {
        "function": {
            "name": "navigate_to_screen",
            "description": "Navigate to app screen.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "screen_name": {"type": "STRING", "description": "Screen to navigate to"}
                },
                "required": ["screen_name"]
            }
        }
    },
    {
        "function": {
            "name": "go_home",
            "description": "Go to home/dashboard.",
            "parameters": {
                "type": "OBJECT",
                "properties": {},
                "required": []
            }
        }
    },
    {
        "function": {
            "name": "update_product_price",
            "description": "Update the selling or buying price of a product.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "product_name": {"type": "STRING", "description": "The name of the product"},
                    "new_selling_price": {"type": "NUMBER", "description": "New selling price in Kwacha"},
                    "new_buying_price": {"type": "NUMBER", "description": "New buying price in Kwacha"}
                },
                "required": ["product_name"]
            }
        }
    }
]

def get_current_datetime():
    """Get current datetime for developer message."""
    return datetime.now().strftime("%Y-%m-%dT%H:%M:%S")

def generate_sale_examples() -> List[Dict[str, Any]]:
    """Generate sale recording examples."""
    examples = []

    variations = [
        ("Sell {qty} {product}", lambda p, q: {"product_name": p, "quantity": q}),
        ("Sold {qty} {product}", lambda p, q: {"product_name": p, "quantity": q}),
        ("Record sale of {qty} {product}", lambda p, q: {"product_name": p, "quantity": q}),
        ("{qty} {product} sold", lambda p, q: {"product_name": p, "quantity": q}),
        ("I sold {qty} {product}", lambda p, q: {"product_name": p, "quantity": q}),
        ("Customer bought {qty} {product}", lambda p, q: {"product_name": p, "quantity": q}),
        ("Sell {product}", lambda p, _: {"product_name": p, "quantity": 1}),
    ]

    for _ in range(200):
        product = random.choice(ALL_PRODUCTS)
        qty = random.randint(1, 20)
        template, args_func = random.choice(variations)

        user_input = template.format(qty=qty, product=product)
        args = args_func(product, qty)

        examples.append({
            "metadata": random.choice(["train", "eval"]),
            "tools": DUKAAI_TOOLS,
            "messages": [
                {
                    "role": "developer",
                    "content": f"Current date and time given in YYYY-MM-DDTHH:MM:SS format: {get_current_datetime()}\nYou are a voice assistant for a Zambian retail shop management system.\n"
                },
                {
                    "role": "user",
                    "content": user_input
                },
                {
                    "role": "assistant",
                    "tool_calls": [{"function": {"name": "record_sale", "arguments": args}}]
                }
            ]
        })

    return examples

def generate_credit_sale_examples() -> List[Dict[str, Any]]:
    """Generate credit sale examples."""
    examples = []

    variations = [
        ("{customer} bought {qty} {product} on credit", True),
        ("{customer} took {qty} {product} pa ng'ong'ole", True),
        ("Record credit sale: {qty} {product} to {customer}", True),
        ("{qty} {product} to {customer} on credit", True),
        ("{customer} wants {qty} {product} on credit", True),
    ]

    for _ in range(150):
        customer = random.choice(CUSTOMER_NAMES)
        product = random.choice(ALL_PRODUCTS)
        qty = random.randint(1, 15)
        template, _ = random.choice(variations)

        user_input = template.format(customer=customer, qty=qty, product=product)

        examples.append({
            "metadata": random.choice(["train", "eval"]),
            "tools": DUKAAI_TOOLS,
            "messages": [
                {
                    "role": "developer",
                    "content": f"Current date and time given in YYYY-MM-DDTHH:MM:SS format: {get_current_datetime()}\nYou are a voice assistant for a Zambian retail shop management system.\n"
                },
                {
                    "role": "user",
                    "content": user_input
                },
                {
                    "role": "assistant",
                    "tool_calls": [{
                        "function": {
                            "name": "record_credit_sale",
                            "arguments": {
                                "customer_name": customer,
                                "product_name": product,
                                "quantity": qty
                            }
                        }
                    }]
                }
            ]
        })

    return examples

def generate_stock_check_examples() -> List[Dict[str, Any]]:
    """Generate stock checking examples."""
    examples = []

    variations = [
        "How many {product} in stock?",
        "Check stock of {product}",
        "How much {product} do I have?",
        "What's the stock level for {product}?",
        "Do I have {product} in stock?",
        "Stock for {product}",
        "How many {product} remaining?",
        "Check {product} inventory",
    ]

    for _ in range(130):
        product = random.choice(ALL_PRODUCTS)
        template = random.choice(variations)
        user_input = template.format(product=product)

        examples.append({
            "metadata": random.choice(["train", "eval"]),
            "tools": DUKAAI_TOOLS,
            "messages": [
                {
                    "role": "developer",
                    "content": f"Current date and time given in YYYY-MM-DDTHH:MM:SS format: {get_current_datetime()}\nYou are a voice assistant for a Zambian retail shop management system.\n"
                },
                {
                    "role": "user",
                    "content": user_input
                },
                {
                    "role": "assistant",
                    "tool_calls": [{
                        "function": {
                            "name": "check_stock",
                            "arguments": {"product_name": product}
                        }
                    }]
                }
            ]
        })

    return examples

def generate_customer_balance_examples() -> List[Dict[str, Any]]:
    """Generate customer balance check examples."""
    examples = []

    variations = [
        "How much does {customer} owe?",
        "What's {customer}'s balance?",
        "{customer}'s debt",
        "Check balance for {customer}",
        "How much does {customer} owe me?",
        "What does {customer} owe?",
        "{customer} balance",
        "Check {customer}'s credit",
    ]

    for _ in range(130):
        customer = random.choice(CUSTOMER_NAMES)
        template = random.choice(variations)
        user_input = template.format(customer=customer)

        examples.append({
            "metadata": random.choice(["train", "eval"]),
            "tools": DUKAAI_TOOLS,
            "messages": [
                {
                    "role": "developer",
                    "content": f"Current date and time given in YYYY-MM-DDTHH:MM:SS format: {get_current_datetime()}\nYou are a voice assistant for a Zambian retail shop management system.\n"
                },
                {
                    "role": "user",
                    "content": user_input
                },
                {
                    "role": "assistant",
                    "tool_calls": [{
                        "function": {
                            "name": "get_customer_balance",
                            "arguments": {"customer_name": customer}
                        }
                    }]
                }
            ]
        })

    return examples

def generate_payment_examples() -> List[Dict[str, Any]]:
    """Generate payment recording examples."""
    examples = []

    variations = [
        ("{customer} paid {amount}", lambda c, a: {"customer_name": c, "amount": a}),
        ("Received {amount} from {customer}", lambda c, a: {"customer_name": c, "amount": a}),
        ("{customer} gave me {amount}", lambda c, a: {"customer_name": c, "amount": a}),
        ("Payment of {amount} from {customer}", lambda c, a: {"customer_name": c, "amount": a}),
        ("Record payment: {customer} {amount}", lambda c, a: {"customer_name": c, "amount": a}),
        ("{amount} from {customer}", lambda c, a: {"customer_name": c, "amount": a}),
    ]

    for _ in range(150):
        customer = random.choice(CUSTOMER_NAMES)
        amount = random.choice([50, 100, 150, 200, 250, 300, 500, 1000])
        template, args_func = random.choice(variations)

        user_input = template.format(customer=customer, amount=amount)
        args = args_func(customer, amount)

        examples.append({
            "metadata": random.choice(["train", "eval"]),
            "tools": DUKAAI_TOOLS,
            "messages": [
                {
                    "role": "developer",
                    "content": f"Current date and time given in YYYY-MM-DDTHH:MM:SS format: {get_current_datetime()}\nYou are a voice assistant for a Zambian retail shop management system.\n"
                },
                {
                    "role": "user",
                    "content": user_input
                },
                {
                    "role": "assistant",
                    "tool_calls": [{
                        "function": {
                            "name": "record_payment",
                            "arguments": args
                        }
                    }]
                }
            ]
        })

    return examples

def generate_add_product_examples() -> List[Dict[str, Any]]:
    """Generate add product examples."""
    examples = []

    variations = [
        ("Add {product} at K{price}", lambda p, pr: {"name": p, "selling_price": pr}),
        ("New product {product} price K{price}", lambda p, pr: {"name": p, "selling_price": pr}),
        ("Create product {product} selling at {price}", lambda p, pr: {"name": p, "selling_price": pr}),
        ("Add new product {product} K{price}", lambda p, pr: {"name": p, "selling_price": pr}),
    ]

    for _ in range(80):
        product = random.choice(ALL_PRODUCTS)
        price = random.choice([5, 10, 15, 20, 25, 30, 50, 100])
        template, args_func = random.choice(variations)

        user_input = template.format(product=product, price=price)
        args = args_func(product, price)

        examples.append({
            "metadata": random.choice(["train", "eval"]),
            "tools": DUKAAI_TOOLS,
            "messages": [
                {
                    "role": "developer",
                    "content": f"Current date and time given in YYYY-MM-DDTHH:MM:SS format: {get_current_datetime()}\nYou are a voice assistant for a Zambian retail shop management system.\n"
                },
                {
                    "role": "user",
                    "content": user_input
                },
                {
                    "role": "assistant",
                    "tool_calls": [{
                        "function": {
                            "name": "add_product",
                            "arguments": args
                        }
                    }]
                }
            ]
        })

    return examples

def generate_update_stock_examples() -> List[Dict[str, Any]]:
    """Generate stock update examples."""
    examples = []

    variations = [
        ("Add {qty} {product} to stock", lambda p, q: {"product_name": p, "quantity": q, "reason": "restock"}),
        ("Restock {qty} {product}", lambda p, q: {"product_name": p, "quantity": q, "reason": "restock"}),
        ("Update stock: add {qty} {product}", lambda p, q: {"product_name": p, "quantity": q, "reason": "restock"}),
        ("{qty} {product} restocked", lambda p, q: {"product_name": p, "quantity": q, "reason": "restock"}),
    ]

    for _ in range(80):
        product = random.choice(ALL_PRODUCTS)
        qty = random.randint(10, 100)
        template, args_func = random.choice(variations)

        user_input = template.format(qty=qty, product=product)
        args = args_func(product, qty)

        examples.append({
            "metadata": random.choice(["train", "eval"]),
            "tools": DUKAAI_TOOLS,
            "messages": [
                {
                    "role": "developer",
                    "content": f"Current date and time given in YYYY-MM-DDTHH:MM:SS format: {get_current_datetime()}\nYou are a voice assistant for a Zambian retail shop management system.\n"
                },
                {
                    "role": "user",
                    "content": user_input
                },
                {
                    "role": "assistant",
                    "tool_calls": [{
                        "function": {
                            "name": "update_stock",
                            "arguments": args
                        }
                    }]
                }
            ]
        })

    return examples

def generate_analytics_examples() -> List[Dict[str, Any]]:
    """Generate analytics examples."""
    examples = []

    periods = ["today", "yesterday", "this_week", "this_month"]

    variations = [
        ("Sales {period}", "get_sales_analytics"),
        ("How much did I make {period}?", "get_revenue_summary"),
        ("Revenue for {period}", "get_revenue_summary"),
        ("Show me {period}'s sales", "get_sales_analytics"),
        ("What are my sales {period}?", "get_sales_analytics"),
        ("{period} analytics", "get_sales_analytics"),
    ]

    for _ in range(150):
        period = random.choice(periods)
        template, func_name = random.choice(variations)
        user_input = template.format(period=period)

        examples.append({
            "metadata": random.choice(["train", "eval"]),
            "tools": DUKAAI_TOOLS,
            "messages": [
                {
                    "role": "developer",
                    "content": f"Current date and time given in YYYY-MM-DDTHH:MM:SS format: {get_current_datetime()}\nYou are a voice assistant for a Zambian retail shop management system.\n"
                },
                {
                    "role": "user",
                    "content": user_input
                },
                {
                    "role": "assistant",
                    "tool_calls": [{
                        "function": {
                            "name": func_name,
                            "arguments": {"period": period}
                        }
                    }]
                }
            ]
        })

    return examples

def generate_customer_management_examples() -> List[Dict[str, Any]]:
    """Generate customer management examples."""
    examples = []

    # Add customer
    for _ in range(90):
        customer = random.choice(CUSTOMER_NAMES)
        phone = f"09{random.randint(60000000, 99999999)}"

        variations = [
            (f"Add customer {customer}", {"name": customer}),
            (f"New customer {customer} phone {phone}", {"name": customer, "phone": phone}),
            (f"Register customer {customer}", {"name": customer}),
            (f"Create customer {customer} {phone}", {"name": customer, "phone": phone}),
        ]

        user_input, args = random.choice(variations)

        examples.append({
            "metadata": random.choice(["train", "eval"]),
            "tools": DUKAAI_TOOLS,
            "messages": [
                {
                    "role": "developer",
                    "content": f"Current date and time given in YYYY-MM-DDTHH:MM:SS format: {get_current_datetime()}\nYou are a voice assistant for a Zambian retail shop management system.\n"
                },
                {
                    "role": "user",
                    "content": user_input
                },
                {
                    "role": "assistant",
                    "tool_calls": [{
                        "function": {
                            "name": "add_customer",
                            "arguments": args
                        }
                    }]
                }
            ]
        })

    return examples

def generate_low_stock_examples() -> List[Dict[str, Any]]:
    """Generate low stock alert examples."""
    examples = []

    variations = [
        "Show me low stock items",
        "What's running low?",
        "Low stock alerts",
        "Which products are low?",
        "Stock alerts",
        "What do I need to restock?",
        "Show low inventory",
    ]

    for _ in range(90):
        user_input = random.choice(variations)

        examples.append({
            "metadata": random.choice(["train", "eval"]),
            "tools": DUKAAI_TOOLS,
            "messages": [
                {
                    "role": "developer",
                    "content": f"Current date and time given in YYYY-MM-DDTHH:MM:SS format: {get_current_datetime()}\nYou are a voice assistant for a Zambian retail shop management system.\n"
                },
                {
                    "role": "user",
                    "content": user_input
                },
                {
                    "role": "assistant",
                    "tool_calls": [{
                        "function": {
                            "name": "get_low_stock_alerts",
                            "arguments": {}
                        }
                    }]
                }
            ]
        })

    return examples

def generate_navigation_examples() -> List[Dict[str, Any]]:
    """Generate navigation examples."""
    examples = []

    screens = {
        "home": ["Go home", "Take me home", "Home screen", "Dashboard"],
        "inventory": ["Open inventory", "Show inventory", "Go to inventory"],
        "sales": ["Open sales", "Show sales", "Sales screen"],
        "customers": ["Show customers", "Customer list", "Open customers"],
        "analytics": ["Show analytics", "Open reports", "Analytics screen"],
    }

    for screen, variations in screens.items():
        for _ in range(20):
            user_input = random.choice(variations)

            examples.append({
                "metadata": random.choice(["train", "eval"]),
                "tools": DUKAAI_TOOLS,
                "messages": [
                    {
                        "role": "developer",
                        "content": f"Current date and time given in YYYY-MM-DDTHH:MM:SS format: {get_current_datetime()}\nYou are a voice assistant for a Zambian retail shop management system.\n"
                    },
                    {
                        "role": "user",
                        "content": user_input
                    },
                    {
                        "role": "assistant",
                        "tool_calls": [{
                            "function": {
                                "name": "go_home" if screen == "home" else "navigate_to_screen",
                                "arguments": {} if screen == "home" else {"screen_name": screen}
                            }
                        }]
                    }
                ]
            })

    return examples

def generate_batch_sale_examples() -> List[Dict[str, Any]]:
    """Generate batch sale examples."""
    examples = []

    for _ in range(80):
        num_items = random.randint(2, 4)
        items = []
        for _ in range(num_items):
            product = random.choice(ALL_PRODUCTS)
            qty = random.randint(1, 10)
            items.append(f"{product}:{qty}")

        items_str = ",".join(items)

        variations = [
            f"Sell {items_str}",
            f"Record sale: {items_str}",
            f"Customer bought {items_str}",
        ]

        user_input = random.choice(variations)

        examples.append({
            "metadata": random.choice(["train", "eval"]),
            "tools": DUKAAI_TOOLS,
            "messages": [
                {
                    "role": "developer",
                    "content": f"Current date and time given in YYYY-MM-DDTHH:MM:SS format: {get_current_datetime()}\nYou are a voice assistant for a Zambian retail shop management system.\n"
                },
                {
                    "role": "user",
                    "content": user_input
                },
                {
                    "role": "assistant",
                    "tool_calls": [{
                        "function": {
                            "name": "record_batch_sale",
                            "arguments": {"items": items_str}
                        }
                    }]
                }
            ]
        })

    return examples

def generate_product_search_examples() -> List[Dict[str, Any]]:
    """Generate product search examples."""
    examples = []

    categories = list(PRODUCTS.keys())

    for _ in range(70):
        if random.random() < 0.6:
            # Search by product name
            product = random.choice(ALL_PRODUCTS)
            query = product[:3]  # Partial match
            variations = [
                f"Search for {query}",
                f"Find {query}",
                f"Look for {query}",
                f"Search products {query}",
            ]
        else:
            # Search by category
            category = random.choice(categories)
            variations = [
                f"Show me {category}",
                f"List {category}",
                f"All {category} products",
            ]
            query = category

        user_input = random.choice(variations)

        examples.append({
            "metadata": random.choice(["train", "eval"]),
            "tools": DUKAAI_TOOLS,
            "messages": [
                {
                    "role": "developer",
                    "content": f"Current date and time given in YYYY-MM-DDTHH:MM:SS format: {get_current_datetime()}\nYou are a voice assistant for a Zambian retail shop management system.\n"
                },
                {
                    "role": "user",
                    "content": user_input
                },
                {
                    "role": "assistant",
                    "tool_calls": [{
                        "function": {
                            "name": "search_products",
                            "arguments": {"query": query}
                        }
                    }]
                }
            ]
        })

    return examples

def generate_top_selling_examples() -> List[Dict[str, Any]]:
    """Generate top selling product examples."""
    examples = []

    periods = ["today", "this_week", "this_month", "all_time"]

    variations = [
        "Top selling products {period}",
        "Best sellers {period}",
        "What sold the most {period}?",
        "Show me top products {period}",
        "Best performing products {period}",
    ]

    for _ in range(70):
        period = random.choice(periods)
        template = random.choice(variations)
        user_input = template.format(period=period)

        examples.append({
            "metadata": random.choice(["train", "eval"]),
            "tools": DUKAAI_TOOLS,
            "messages": [
                {
                    "role": "developer",
                    "content": f"Current date and time given in YYYY-MM-DDTHH:MM:SS format: {get_current_datetime()}\nYou are a voice assistant for a Zambian retail shop management system.\n"
                },
                {
                    "role": "user",
                    "content": user_input
                },
                {
                    "role": "assistant",
                    "tool_calls": [{
                        "function": {
                            "name": "get_top_selling_products",
                            "arguments": {"period": period}
                        }
                    }]
                }
            ]
        })

    return examples

def generate_overdue_credits_examples() -> List[Dict[str, Any]]:
    """Generate overdue credits examples."""
    examples = []

    variations = [
        "Show me overdue credits",
        "Who hasn't paid?",
        "Overdue accounts",
        "Which customers owe overdue money?",
        "Late payments",
        "Overdue balances",
    ]

    for _ in range(60):
        user_input = random.choice(variations)

        examples.append({
            "metadata": random.choice(["train", "eval"]),
            "tools": DUKAAI_TOOLS,
            "messages": [
                {
                    "role": "developer",
                    "content": f"Current date and time given in YYYY-MM-DDTHH:MM:SS format: {get_current_datetime()}\nYou are a voice assistant for a Zambian retail shop management system.\n"
                },
                {
                    "role": "user",
                    "content": user_input
                },
                {
                    "role": "assistant",
                    "tool_calls": [{
                        "function": {
                            "name": "get_overdue_credits",
                            "arguments": {}
                        }
                    }]
                }
            ]
        })

    return examples

def generate_today_sales_examples() -> List[Dict[str, Any]]:
    """Generate today's sales examples."""
    examples = []

    variations = [
        "Today's sales",
        "Show me today's sales",
        "How much did I sell today?",
        "Sales for today",
        "What did I sell today?",
        "Today sales summary",
    ]

    for _ in range(60):
        user_input = random.choice(variations)

        examples.append({
            "metadata": random.choice(["train", "eval"]),
            "tools": DUKAAI_TOOLS,
            "messages": [
                {
                    "role": "developer",
                    "content": f"Current date and time given in YYYY-MM-DDTHH:MM:SS format: {get_current_datetime()}\nYou are a voice assistant for a Zambian retail shop management system.\n"
                },
                {
                    "role": "user",
                    "content": user_input
                },
                {
                    "role": "assistant",
                    "tool_calls": [{
                        "function": {
                            "name": "get_today_sales",
                            "arguments": {}
                        }
                    }]
                }
            ]
        })

    return examples

def generate_all_credit_balances_examples() -> List[Dict[str, Any]]:
    """Generate all credit balances examples."""
    examples = []

    variations = [
        "Show all credits",
        "All outstanding balances",
        "Who owes me money?",
        "List all debts",
        "Credit balances",
        "All customer balances",
    ]

    for _ in range(60):
        user_input = random.choice(variations)

        examples.append({
            "metadata": random.choice(["train", "eval"]),
            "tools": DUKAAI_TOOLS,
            "messages": [
                {
                    "role": "developer",
                    "content": f"Current date and time given in YYYY-MM-DDTHH:MM:SS format: {get_current_datetime()}\nYou are a voice assistant for a Zambian retail shop management system.\n"
                },
                {
                    "role": "user",
                    "content": user_input
                },
                {
                    "role": "assistant",
                    "tool_calls": [{
                        "function": {
                            "name": "get_all_credit_balances",
                            "arguments": {}
                        }
                    }]
                }
            ]
        })

    return examples

def generate_update_price_examples() -> List[Dict[str, Any]]:
    """Generate price update examples."""
    examples = []

    for _ in range(70):
        product = random.choice(ALL_PRODUCTS)
        new_price = random.choice([10, 15, 20, 25, 30, 50, 100])

        variations = [
            (f"Change {product} price to K{new_price}", {"product_name": product, "new_selling_price": new_price}),
            (f"Update {product} price K{new_price}", {"product_name": product, "new_selling_price": new_price}),
            (f"Set {product} price to {new_price}", {"product_name": product, "new_selling_price": new_price}),
            (f"{product} new price K{new_price}", {"product_name": product, "new_selling_price": new_price}),
        ]

        user_input, args = random.choice(variations)

        examples.append({
            "metadata": random.choice(["train", "eval"]),
            "tools": DUKAAI_TOOLS,
            "messages": [
                {
                    "role": "developer",
                    "content": f"Current date and time given in YYYY-MM-DDTHH:MM:SS format: {get_current_datetime()}\nYou are a voice assistant for a Zambian retail shop management system.\n"
                },
                {
                    "role": "user",
                    "content": user_input
                },
                {
                    "role": "assistant",
                    "tool_calls": [{
                        "function": {
                            "name": "update_product_price",
                            "arguments": args
                        }
                    }]
                }
            ]
        })

    return examples

def generate_out_of_stock_examples() -> List[Dict[str, Any]]:
    """Generate out of stock examples."""
    examples = []

    variations = [
        "What's out of stock?",
        "Show me out of stock items",
        "Which products are finished?",
        "Out of stock products",
        "Zero stock items",
    ]

    for _ in range(50):
        user_input = random.choice(variations)

        examples.append({
            "metadata": random.choice(["train", "eval"]),
            "tools": DUKAAI_TOOLS,
            "messages": [
                {
                    "role": "developer",
                    "content": f"Current date and time given in YYYY-MM-DDTHH:MM:SS format: {get_current_datetime()}\nYou are a voice assistant for a Zambian retail shop management system.\n"
                },
                {
                    "role": "user",
                    "content": user_input
                },
                {
                    "role": "assistant",
                    "tool_calls": [{
                        "function": {
                            "name": "get_out_of_stock",
                            "arguments": {}
                        }
                    }]
                }
            ]
        })

    return examples

def generate_product_details_examples() -> List[Dict[str, Any]]:
    """Generate product details examples."""
    examples = []

    for _ in range(70):
        product = random.choice(ALL_PRODUCTS)

        variations = [
            f"Show {product} details",
            f"Info about {product}",
            f"Details for {product}",
            f"Tell me about {product}",
            f"{product} information",
        ]

        user_input = random.choice(variations)

        examples.append({
            "metadata": random.choice(["train", "eval"]),
            "tools": DUKAAI_TOOLS,
            "messages": [
                {
                    "role": "developer",
                    "content": f"Current date and time given in YYYY-MM-DDTHH:MM:SS format: {get_current_datetime()}\nYou are a voice assistant for a Zambian retail shop management system.\n"
                },
                {
                    "role": "user",
                    "content": user_input
                },
                {
                    "role": "assistant",
                    "tool_calls": [{
                        "function": {
                            "name": "get_product_details",
                            "arguments": {"product_name": product}
                        }
                    }]
                }
            ]
        })

    return examples

def generate_customer_details_examples() -> List[Dict[str, Any]]:
    """Generate customer details examples."""
    examples = []

    for _ in range(60):
        customer = random.choice(CUSTOMER_NAMES)

        variations = [
            f"Show {customer}'s details",
            f"Info about {customer}",
            f"{customer} information",
            f"Customer details for {customer}",
        ]

        user_input = random.choice(variations)

        examples.append({
            "metadata": random.choice(["train", "eval"]),
            "tools": DUKAAI_TOOLS,
            "messages": [
                {
                    "role": "developer",
                    "content": f"Current date and time given in YYYY-MM-DDTHH:MM:SS format: {get_current_datetime()}\nYou are a voice assistant for a Zambian retail shop management system.\n"
                },
                {
                    "role": "user",
                    "content": user_input
                },
                {
                    "role": "assistant",
                    "tool_calls": [{
                        "function": {
                            "name": "get_customer_details",
                            "arguments": {"customer_name": customer}
                        }
                    }]
                }
            ]
        })

    return examples

def main():
    """Generate all training examples and save to JSONL."""
    print("Generating DukaAI FunctionGemma training dataset...")

    all_examples = []

    # Generate examples from each category
    generators = [
        ("Sales", generate_sale_examples),
        ("Credit Sales", generate_credit_sale_examples),
        ("Stock Checks", generate_stock_check_examples),
        ("Customer Balance", generate_customer_balance_examples),
        ("Payments", generate_payment_examples),
        ("Add Products", generate_add_product_examples),
        ("Update Stock", generate_update_stock_examples),
        ("Analytics", generate_analytics_examples),
        ("Customer Management", generate_customer_management_examples),
        ("Low Stock", generate_low_stock_examples),
        ("Navigation", generate_navigation_examples),
        ("Batch Sales", generate_batch_sale_examples),
        ("Product Search", generate_product_search_examples),
        ("Top Selling", generate_top_selling_examples),
        ("Overdue Credits", generate_overdue_credits_examples),
        ("Today Sales", generate_today_sales_examples),
        ("All Credit Balances", generate_all_credit_balances_examples),
        ("Update Prices", generate_update_price_examples),
        ("Out of Stock", generate_out_of_stock_examples),
        ("Product Details", generate_product_details_examples),
        ("Customer Details", generate_customer_details_examples),
    ]

    for name, generator in generators:
        examples = generator()
        all_examples.extend(examples)
        print(f"Generated {len(examples)} {name} examples")

    # Shuffle to mix train/eval
    random.shuffle(all_examples)

    # Write to JSONL file
    output_file = "dukaai_functiongemma_training_data.jsonl"
    with open(output_file, 'w', encoding='utf-8') as f:
        for example in all_examples:
            f.write(json.dumps(example, ensure_ascii=False) + '\n')

    # Statistics
    total = len(all_examples)
    train_count = sum(1 for ex in all_examples if ex['metadata'] == 'train')
    eval_count = sum(1 for ex in all_examples if ex['metadata'] == 'eval')

    print(f"\n{'='*60}")
    print(f"Dataset generation complete!")
    print(f"{'='*60}")
    print(f"Total examples: {total}")
    print(f"Training examples: {train_count}")
    print(f"Evaluation examples: {eval_count}")
    print(f"Output file: {output_file}")
    print(f"{'='*60}")

if __name__ == "__main__":
    main()

// Application State
let currentPage = 0;
let currentKeyword = '';
let cart = [];
let currentScreen = 'products';

// DOM Elements - Main
const loginScreen = document.getElementById('login-screen');
const mainScreen = document.getElementById('main-screen');
const loginForm = document.getElementById('login-form');
const userName = document.getElementById('user-name');

// DOM Elements - Products
const searchInput = document.getElementById('search-input');
const searchBtn = document.getElementById('search-btn');
const productsGrid = document.getElementById('products-grid');
const paginationControls = document.getElementById('pagination-controls');
const paginationInfo = document.getElementById('pagination-info');
const loadingSpinner = document.getElementById('loading-spinner');

// DOM Elements - Cart
const cartItems = document.getElementById('cart-items');
const orderBtn = document.getElementById('order-btn');

// DOM Elements - Orders
const ordersContainer = document.getElementById('orders-container');

// Screen Elements
const productsScreen = document.getElementById('products-screen');
const ordersScreen = document.getElementById('orders-screen');

// Initialize Application
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

function initializeApp() {
    // Check authentication state
    checkAuthentication();

    // Set up event listeners
    setupEventListeners();
}

// Authentication Management
function checkAuthentication() {
    if (ApiService.isAuthenticated()) {
        showMainScreen();
        loadInitialData();
    } else {
        showLoginScreen();
    }
}

function showLoginScreen() {
    loginScreen.classList.add('show');
    mainScreen.classList.remove('show');
}

function showMainScreen() {
    loginScreen.classList.remove('show');
    mainScreen.classList.add('show');

    // Update user name in navbar
    const user = ApiService.getCurrentUser();
    if (user && userName) {
        userName.textContent = user.username || 'User';
    }
}

function showScreen(screenName) {
    // Update navigation
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
    });

    // Show selected screen
    if (screenName === 'products') {
        productsScreen.classList.remove('d-none');
        ordersScreen.classList.add('d-none');
        document.querySelector('a[onclick*="products"]').classList.add('active');
        currentScreen = 'products';
        loadProducts();
    } else if (screenName === 'orders') {
        productsScreen.classList.add('d-none');
        ordersScreen.classList.remove('d-none');
        document.querySelector('a[onclick*="orders"]').classList.add('active');
        currentScreen = 'orders';
        loadUserOrders();
    }
}

function loadInitialData() {
    // Load products by default
    showScreen('products');
}

// Setup Event Listeners
function setupEventListeners() {
    // Login form
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }

    // Search functionality
    if (searchBtn) {
        searchBtn.addEventListener('click', handleSearch);
    }
    if (searchInput) {
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                handleSearch();
            }
        });
    }

    // Order button
    if (orderBtn) {
        orderBtn.addEventListener('click', handleOrder);
    }
}

// User Authentication functions are now handled in handleLogin()

// Product Search and Display
async function handleSearch() {
    currentKeyword = searchInput.value.trim();
    currentPage = 0;
    await loadProducts();
}

async function loadProducts() {
    try {
        showLoading(true);

        const response = await ApiService.searchProducts(currentKeyword, currentPage, 10);

        displayProducts(response.content);
        displayPagination(response);

    } catch (error) {
        console.error('Error loading products:', error);
        showError('Không thể tải sản phẩm. Vui lòng thử lại.');
    } finally {
        showLoading(false);
    }
}

function displayProducts(products) {
    productsGrid.innerHTML = '';

    if (products.length === 0) {
        productsGrid.innerHTML = `
            <div class="col-12 text-center">
                <p class="text-muted">Không tìm thấy sản phẩm nào.</p>
            </div>
        `;
        return;
    }

    products.forEach(product => {
        const productCard = createProductCard(product);
        productsGrid.appendChild(productCard);
    });
}

function createProductCard(product) {
    const col = document.createElement('div');
    col.className = 'col-md-6 col-lg-4';

    col.innerHTML = `
        <div class="product-card">
            <h5 class="card-title">${product.name}</h5>
            <p class="card-text text-muted">${product.description}</p>
            <div class="product-price">₫${product.price.toLocaleString('vi-VN')}</div>
            <div class="product-stock">Còn lại: ${product.stockQuantity}</div>
            <button class="btn btn-primary btn-add-cart"
                    onclick="addToCart(${product.id}, '${product.name}', ${product.price}, ${product.stockQuantity})"
                    ${product.stockQuantity <= 0 ? 'disabled' : ''}>
                <i class="fas fa-cart-plus"></i>
                ${product.stockQuantity <= 0 ? 'Hết hàng' : 'Thêm vào giỏ'}
            </button>
        </div>
    `;

    return col;
}

// Pagination
function displayPagination(response) {
    const { pageNumber, pageSize, totalElements, totalPages, first, last } = response;

    // Update pagination info
    paginationInfo.textContent = `Hiển thị ${pageNumber * pageSize + 1}-${Math.min((pageNumber + 1) * pageSize, totalElements)} trong tổng số ${totalElements} sản phẩm`;

    // Create pagination controls
    paginationControls.innerHTML = '';

    if (totalPages <= 1) return;

    // Previous button
    if (!first) {
        const prevBtn = createPaginationButton('Trước', pageNumber - 1);
        paginationControls.appendChild(prevBtn);
    }

    // Page numbers
    const startPage = Math.max(0, pageNumber - 2);
    const endPage = Math.min(totalPages - 1, pageNumber + 2);

    for (let i = startPage; i <= endPage; i++) {
        const pageBtn = createPaginationButton((i + 1).toString(), i);
        if (i === pageNumber) {
            pageBtn.classList.add('active');
        }
        paginationControls.appendChild(pageBtn);
    }

    // Next button
    if (!last) {
        const nextBtn = createPaginationButton('Sau', pageNumber + 1);
        paginationControls.appendChild(nextBtn);
    }
}

function createPaginationButton(text, page) {
    const button = document.createElement('button');
    button.className = 'pagination-btn';
    button.textContent = text;
    button.addEventListener('click', () => {
        currentPage = page;
        loadProducts();
    });
    return button;
}

// Cart Functionality
function addToCart(productId, productName, price, maxStock) {
    const existingItem = cart.find(item => item.productId === productId);

    if (existingItem) {
        if (existingItem.quantity < maxStock) {
            existingItem.quantity += 1;
        } else {
            showError(`Không thể thêm. Chỉ còn ${maxStock} sản phẩm trong kho.`);
            return;
        }
    } else {
        cart.push({
            productId: productId,
            name: productName,
            price: price,
            quantity: 1,
            maxStock: maxStock
        });
    }

    updateCartDisplay();
    showSuccess('Đã thêm sản phẩm vào giỏ hàng!');
}

function updateCartDisplay() {
    cartItems.innerHTML = '';

    if (cart.length === 0) {
        cartItems.innerHTML = `
            <div class="empty-cart">
                <i class="fas fa-shopping-cart fa-2x mb-2"></i>
                <p>Giỏ hàng trống</p>
            </div>
        `;
        orderBtn.disabled = true;
        return;
    }

    cart.forEach((item, index) => {
        const cartItem = createCartItem(item, index);
        cartItems.appendChild(cartItem);
    });

    orderBtn.disabled = false;
}

function createCartItem(item, index) {
    const div = document.createElement('div');
    div.className = 'cart-item';

    div.innerHTML = `
        <div class="cart-item-info">
            <div class="fw-bold">${item.name}</div>
            <div class="text-muted">₫${item.price.toLocaleString('vi-VN')}</div>
        </div>
        <div class="cart-item-controls">
            <div class="quantity-controls">
                <button onclick="changeQuantity(${index}, -1)" ${item.quantity <= 1 ? 'disabled' : ''}>-</button>
                <input type="number" value="${item.quantity}" readonly min="1" max="${item.maxStock}">
                <button onclick="changeQuantity(${index}, 1)" ${item.quantity >= item.maxStock ? 'disabled' : ''}>+</button>
            </div>
            <button class="btn btn-sm btn-outline-danger ms-2" onclick="removeFromCart(${index})">
                <i class="fas fa-trash"></i>
            </button>
        </div>
    `;

    return div;
}

function changeQuantity(index, delta) {
    const item = cart[index];
    const newQuantity = item.quantity + delta;

    if (newQuantity >= 1 && newQuantity <= item.maxStock) {
        item.quantity = newQuantity;
        updateCartDisplay();
    }
}

function removeFromCart(index) {
    cart.splice(index, 1);
    updateCartDisplay();
}

// Order Submission
async function handleOrder() {
    if (cart.length === 0) {
        showError('Giỏ hàng trống!');
        return;
    }

    if (!ApiService.isAuthenticated()) {
        showError('Vui lòng đăng nhập để đặt hàng!');
        return;
    }

    try {
        // Prepare order items
        const orderItems = cart.map(item => ({
            productId: item.productId,
            quantity: item.quantity
        }));

        // Create order
        const orderResponse = await ApiService.createOrder(orderItems);

        showSuccess('Đặt hàng thành công!');
        cart = [];
        updateCartDisplay();

        console.log('Order created:', orderResponse);

    } catch (error) {
        console.error('Order error:', error);
        showError('Không thể đặt hàng: ' + error.message);
    }
}

// Utility Functions
function showLoading(show) {
    loadingSpinner.classList.toggle('d-none', !show);
}

function showError(message) {
    // Simple alert for now, can be replaced with toast notifications
    alert('Lỗi: ' + message);
}

function showSuccess(message) {
    // Simple alert for now, can be replaced with toast notifications
    alert('Thành công: ' + message);
}

// Login/Logout Functions
async function handleLogin(e) {
    e.preventDefault();

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    if (!username || !password) {
        showError('Vui lòng nhập đầy đủ thông tin đăng nhập');
        return;
    }

    try {
        // For demo purposes, simulate login
        // In real app, this would call ApiService.login()
        const demoUser = {
            id: 1,
            username: username,
            email: username + '@example.com'
        };

        ApiService.setCurrentUser(demoUser);

        showSuccess('Đăng nhập thành công!');
        showMainScreen();
        loadInitialData();

    } catch (error) {
        console.error('Login error:', error);
        showError('Đăng nhập thất bại: ' + error.message);
    }
}

function logout() {
    if (confirm('Bạn có chắc chắn muốn đăng xuất?')) {
        ApiService.logout();
        cart = []; // Clear cart
        currentPage = 0;
        currentKeyword = '';
        showLoginScreen();
        showSuccess('Đã đăng xuất thành công');
    }
}

// Order History Functions
async function loadUserOrders() {
    if (!ApiService.isAuthenticated()) {
        showError('Vui lòng đăng nhập để xem đơn hàng');
        return;
    }

    try {
        const ordersContainer = document.getElementById('orders-container');
        ordersContainer.innerHTML = '<div class="text-center"><div class="spinner-border text-primary"></div><p class="mt-2">Đang tải đơn hàng...</p></div>';

        const orders = await ApiService.getUserOrders();

        // Enrich orders with product names
        const enrichedOrders = await Promise.all(orders.map(async (order) => {
            const enrichedItems = await Promise.all(order.items.map(async (item) => {
                try {
                    const product = await ApiService.getProduct(item.productId);
                    return {
                        ...item,
                        productName: product.name
                    };
                } catch (error) {
                    console.warn('Could not load product', item.productId, error);
                    return {
                        ...item,
                        productName: `Product ${item.productId}`
                    };
                }
            }));

            return {
                ...order,
                items: enrichedItems
            };
        }));

        displayOrders(enrichedOrders);

    } catch (error) {
        console.error('Error loading orders:', error);
        showError('Không thể tải đơn hàng: ' + error.message);

        // Show empty state on error
        const ordersContainer = document.getElementById('orders-container');
        ordersContainer.innerHTML = `
            <div class="text-center text-muted">
                <i class="fas fa-exclamation-triangle fa-3x mb-3"></i>
                <h5>Không thể tải đơn hàng</h5>
                <p>Vui lòng thử lại sau.</p>
            </div>
        `;
    }
}

function displayOrders(orders) {
    const ordersContainer = document.getElementById('orders-container');
    if (!ordersContainer) return;

    if (!orders || orders.length === 0) {
        ordersContainer.innerHTML = `
            <div class="text-center text-muted">
                <i class="fas fa-shopping-cart fa-3x mb-3"></i>
                <h5>Bạn chưa có đơn hàng nào</h5>
                <p>Hãy bắt đầu mua sắm để tạo đơn hàng đầu tiên!</p>
                <button class="btn btn-primary" onclick="showScreen('products')">
                    <i class="fas fa-box"></i> Xem sản phẩm
                </button>
            </div>
        `;
        return;
    }

    ordersContainer.innerHTML = orders.map(order => `
        <div class="card mb-3">
            <div class="card-header d-flex justify-content-between align-items-center">
                <h6 class="mb-0">
                    <i class="fas fa-receipt"></i> Đơn hàng #${order.id}
                </h6>
                <span class="badge bg-${getStatusColor(order.status)}">${getStatusText(order.status)}</span>
            </div>
            <div class="card-body">
                <div class="row">
                    <div class="col-md-8">
                        <h6>Sản phẩm:</h6>
                        <ul class="list-unstyled">
                            ${order.items.map(item => `
                                <li>${item.productName || `Product ${item.productId}`} x${item.quantity} - ₫${parseFloat(item.price).toLocaleString('vi-VN')}</li>
                            `).join('')}
                        </ul>
                    </div>
                    <div class="col-md-4 text-end">
                        <p class="mb-1"><strong>Tổng tiền:</strong></p>
                        <h5 class="text-primary">₫${parseFloat(order.totalAmount).toLocaleString('vi-VN')}</h5>
                    </div>
                </div>
            </div>
        </div>
    `).join('');
}

function getStatusColor(status) {
    switch (status) {
        case 'CREATED': return 'secondary';
        case 'PENDING_PAYMENT': return 'warning';
        case 'PENDING_SHIPMENT': return 'info';
        case 'COMPLETED': return 'success';
        case 'CANCELLED': return 'danger';
        default: return 'secondary';
    }
}

function getStatusText(status) {
    switch (status) {
        case 'CREATED': return 'Đã tạo';
        case 'PENDING_PAYMENT': return 'Chờ thanh toán';
        case 'PENDING_SHIPMENT': return 'Chờ giao hàng';
        case 'COMPLETED': return 'Hoàn thành';
        case 'CANCELLED': return 'Đã hủy';
        default: return status;
    }
}

// Removed formatDate function as API doesn't return created date

// Global functions for onclick handlers
window.showScreen = showScreen;
window.logout = logout;

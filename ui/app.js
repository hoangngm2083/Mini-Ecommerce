// Application State
let currentPage = 0;
let currentKeyword = '';
let cart = [];
let currentScreen = 'products';
let currentShippingMethod = 'STANDARD'; // Track current shipping method
let currentShippingFee = 0; // Track current shipping fee

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

// DOM Elements - Order Modal
const orderModal = new bootstrap.Modal(document.getElementById('orderModal'));
const orderForm = document.getElementById('order-form');
const orderItemsSummary = document.getElementById('order-items-summary');
const confirmOrderBtn = document.getElementById('confirm-order-btn');

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
        userName.textContent = user.name || user.email || 'User';
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

    // Register form
    const registerForm = document.getElementById('register-form');
    if (registerForm) {
        registerForm.addEventListener('submit', handleRegister);
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

    // Confirm order button
    if (confirmOrderBtn) {
        confirmOrderBtn.addEventListener('click', handleConfirmOrder);
    }

    // Shipment method change listener
    const shipmentMethodSelect = document.getElementById('shipment-method');
    if (shipmentMethodSelect) {
        shipmentMethodSelect.addEventListener('change', handleShipmentMethodChange);
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

    // Populate order summary and show modal
    populateOrderSummary();
    
    // Reset to default shipping method
    currentShippingMethod = 'STANDARD';
    
    // Load initial shipping fee for STANDARD method
    await loadShippingFee('STANDARD');
    
    orderModal.show();
}

async function handleConfirmOrder() {
    // Validate form
    const paymentMethod = document.getElementById('payment-method').value;
    const shipmentMethod = document.getElementById('shipment-method').value;
    const shippingAddress = document.getElementById('shipping-address').value.trim();
    const orderNotes = document.getElementById('order-notes').value;

    // Check required fields
    if (!shippingAddress) {
        showError('Vui lòng nhập địa chỉ giao hàng!');
        document.getElementById('shipping-address').focus();
        return;
    }

    try {
        // Prepare order payload
        const orderPayload = {
            items: cart.map(item => ({
                productId: item.productId,
                quantity: item.quantity
            })),
            payment: {
                method: paymentMethod
            },
            shipment: {
                address: shippingAddress,
                method: shipmentMethod,
                notes: orderNotes
            }
        };

        // Create order
        const orderResponse = await ApiService.createOrder(orderPayload);

        // Hide modal and reset form
        orderModal.hide();
        orderForm.reset();

        showSuccess('Đặt hàng thành công!');
        cart = [];
        updateCartDisplay();

        console.log('Order created:', orderResponse);

    } catch (error) {
        console.error('Order error:', error);
        showError('Không thể đặt hàng: ' + error.message);
    }
}

// Shipping Fee Management
async function loadShippingFee(method) {
    try {
        const response = await ApiService.getShippingFee(method);
        
        // Save current shipping fee
        currentShippingFee = parseFloat(response.fee);
        
        // Update order summary with new shipping fee
        populateOrderSummary();
        
    } catch (error) {
        console.error('Error loading shipping fee:', error);
        currentShippingFee = 0;
        populateOrderSummary();
        showError('Không thể tải phí vận chuyển');
    }
}

async function handleShipmentMethodChange(e) {
    const newMethod = e.target.value;
    
    // Only load if method actually changed
    if (newMethod !== currentShippingMethod) {
        currentShippingMethod = newMethod;
        await loadShippingFee(newMethod);
    }
}

function populateOrderSummary() {
    let subtotal = 0;

    const itemsHtml = cart.map(item => {
        const itemTotal = item.price * item.quantity;
        subtotal += itemTotal;

        return `
            <div class="order-item-summary">
                <div class="order-item-info">
                    <div class="fw-bold">${item.name}</div>
                    <div class="text-muted">Số lượng: ${item.quantity}</div>
                </div>
                <div class="order-item-price">₫${itemTotal.toLocaleString('vi-VN')}</div>
            </div>
        `;
    }).join('');

    // Calculate final total
    const finalTotal = subtotal + currentShippingFee;

    orderItemsSummary.innerHTML = `
        ${itemsHtml}
        <hr class="my-3">
        <div class="d-flex justify-content-between mb-2">
            <span>Tạm tính:</span>
            <span>₫${subtotal.toLocaleString('vi-VN')}</span>
        </div>
        <div class="d-flex justify-content-between mb-2">
            <span>Phí vận chuyển:</span>
            <span class="text-primary" id="shipping-fee-in-summary">
                ${currentShippingFee > 0 
                    ? '₫' + currentShippingFee.toLocaleString('vi-VN')
                    : '<small class="text-muted">Đang tải...</small>'
                }
            </span>
        </div>
        <hr class="my-2">
        <div class="order-total">
            <strong>Tổng cộng: ₫${finalTotal.toLocaleString('vi-VN')}</strong>
        </div>
    `;
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

function isValidEmail(email) {
    // Email validation regex
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

// Login/Logout Functions
async function handleLogin(e) {
    e.preventDefault();

    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;

    if (!email || !password) {
        showError('Vui lòng nhập đầy đủ thông tin đăng nhập');
        return;
    }

    // Validate email format
    if (!isValidEmail(email)) {
        showError('Email không hợp lệ. Vui lòng nhập đúng định dạng email');
        return;
    }

    try {
        // Call actual login API (backend expects email, not username)
        const response = await ApiService.login({
            email: email,
            password: password
        });

        showSuccess('Đăng nhập thành công!');
        showMainScreen();
        loadInitialData();

    } catch (error) {
        console.error('Login error:', error);
        showError('Đăng nhập thất bại: ' + error.message);
    }
}

async function handleRegister(e) {
    e.preventDefault();

    const name = document.getElementById('register-name').value;
    const email = document.getElementById('register-email').value;
    const password = document.getElementById('register-password').value;
    const confirmPassword = document.getElementById('register-confirm-password').value;

    // Validation
    if (!name || !email || !password || !confirmPassword) {
        showError('Vui lòng nhập đầy đủ thông tin');
        return;
    }

    // Validate email format
    if (!isValidEmail(email)) {
        showError('Email không hợp lệ. Vui lòng nhập đúng định dạng email');
        return;
    }

    if (password !== confirmPassword) {
        showError('Mật khẩu xác nhận không khớp');
        return;
    }

    if (password.length < 6) {
        showError('Mật khẩu phải có ít nhất 6 ký tự');
        return;
    }

    try {
        // Call register API with CUSTOMER role hardcoded
        const response = await ApiService.register({
            name: name,
            email: email,
            password: password,
            role: 'CUSTOMER'  // Hardcoded as CUSTOMER
        });

        showSuccess('Đăng ký thành công! Đang đăng nhập...');
        
        // Auto login after successful registration
        showMainScreen();
        loadInitialData();

    } catch (error) {
        console.error('Register error:', error);
        showError('Đăng ký thất bại: ' + error.message);
    }
}

function toggleAuthForm() {
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    const authTitle = document.getElementById('auth-title');
    const toggleText = document.getElementById('toggle-auth-text');
    const loginDemo = document.getElementById('login-demo');

    if (loginForm.classList.contains('d-none')) {
        // Show login form
        loginForm.classList.remove('d-none');
        registerForm.classList.add('d-none');
        authTitle.innerHTML = '<i class="fas fa-sign-in-alt"></i> Đăng nhập';
        toggleText.textContent = 'Chưa có tài khoản? Đăng ký ngay';
        loginDemo.classList.remove('d-none');
    } else {
        // Show register form
        loginForm.classList.add('d-none');
        registerForm.classList.remove('d-none');
        authTitle.innerHTML = '<i class="fas fa-user-plus"></i> Đăng ký';
        toggleText.textContent = 'Đã có tài khoản? Đăng nhập';
        loginDemo.classList.add('d-none');
    }
}

function togglePassword(inputId) {
    const input = document.getElementById(inputId);
    const icon = document.getElementById(inputId + '-icon');
    
    if (input.type === 'password') {
        input.type = 'text';
        icon.classList.remove('fa-eye');
        icon.classList.add('fa-eye-slash');
    } else {
        input.type = 'password';
        icon.classList.remove('fa-eye-slash');
        icon.classList.add('fa-eye');
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
                <div>
                    <span class="badge bg-${getStatusColor(order.status)}">${getStatusText(order.status)}</span>
                    <button class="btn btn-sm btn-outline-primary ms-2" onclick="showOrderDetail(${order.id})">
                        <i class="fas fa-eye"></i> Xem chi tiết
                    </button>
                </div>
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

// Show order detail modal
async function showOrderDetail(orderId) {
    try {
        const orderDetail = await ApiService.getOrderDetail(orderId);
        displayOrderDetailModal(orderDetail);
    } catch (error) {
        console.error('Error loading order detail:', error);
        showError('Không thể tải chi tiết đơn hàng: ' + error.message);
    }
}

function displayOrderDetailModal(orderDetail) {
    // Get or create the modal
    let modalElement = document.getElementById('orderDetailModal');
    if (!modalElement) {
        // Create modal if it doesn't exist
        createOrderDetailModal();
        modalElement = document.getElementById('orderDetailModal');
    }

    // Populate modal content
    const modalBody = modalElement.querySelector('.modal-body');
    
    // Payment section
    let paymentHtml = '<p class="text-muted">Không có thông tin thanh toán</p>';
    if (orderDetail.payment) {
        const payment = orderDetail.payment;
        paymentHtml = `
            <table class="table table-sm">
                <tbody>
                    <tr>
                        <th width="40%">Phương thức:</th>
                        <td>${payment.paymentMethod}</td>
                    </tr>
                    <tr>
                        <th>Số tiền:</th>
                        <td class="text-primary fw-bold">₫${parseFloat(payment.amount).toLocaleString('vi-VN')}</td>
                    </tr>
                    <tr>
                        <th>Trạng thái:</th>
                        <td><span class="badge bg-${getPaymentStatusColor(payment.status)}">${getPaymentStatusText(payment.status)}</span></td>
                    </tr>
                    ${payment.transactionId ? `
                    <tr>
                        <th>Mã giao dịch:</th>
                        <td>${payment.transactionId}</td>
                    </tr>` : ''}
                    ${payment.paidAt ? `
                    <tr>
                        <th>Đã thanh toán lúc:</th>
                        <td>${new Date(payment.paidAt).toLocaleString('vi-VN')}</td>
                    </tr>` : ''}
                </tbody>
            </table>
        `;
    }

    // Shipment section
    let shipmentHtml = '<p class="text-muted">Không có thông tin vận chuyển</p>';
    if (orderDetail.shipment) {
        const shipment = orderDetail.shipment;
        shipmentHtml = `
            <table class="table table-sm">
                <tbody>
                    <tr>
                        <th width="40%">Địa chỉ:</th>
                        <td>${shipment.address || 'N/A'}</td>
                    </tr>
                    <tr>
                        <th>Phương thức:</th>
                        <td>${shipment.method}</td>
                    </tr>
                    <tr>
                        <th>Phí vận chuyển:</th>
                        <td class="text-primary fw-bold">₫${parseFloat(shipment.fee).toLocaleString('vi-VN')}</td>
                    </tr>
                    <tr>
                        <th>Trạng thái:</th>
                        <td><span class="badge bg-${getShipmentStatusColor(shipment.status)}">${getShipmentStatusText(shipment.status)}</span></td>
                    </tr>
                    ${shipment.notes ? `
                    <tr>
                        <th>Ghi chú:</th>
                        <td>${shipment.notes}</td>
                    </tr>` : ''}
                    ${shipment.shippedAt ? `
                    <tr>
                        <th>Bắt đầu giao:</th>
                        <td>${new Date(shipment.shippedAt).toLocaleString('vi-VN')}</td>
                    </tr>` : ''}
                    ${shipment.deliveredAt ? `
                    <tr>
                        <th>Đã giao:</th>
                        <td>${new Date(shipment.deliveredAt).toLocaleString('vi-VN')}</td>
                    </tr>` : ''}
                </tbody>
            </table>
        `;
    }

    modalBody.innerHTML = `
        <div class="mb-4">
            <h6 class="border-bottom pb-2">
                <i class="fas fa-receipt"></i> Thông tin đơn hàng #${orderDetail.id}
            </h6>
            <p class="mb-1"><strong>Trạng thái:</strong> <span class="badge bg-${getStatusColor(orderDetail.status)}">${getStatusText(orderDetail.status)}</span></p>
            <p class="mb-1"><strong>Tổng tiền:</strong> <span class="text-primary fw-bold">₫${parseFloat(orderDetail.totalAmount).toLocaleString('vi-VN')}</span></p>
        </div>

        <div class="mb-4">
            <h6 class="border-bottom pb-2">
                <i class="fas fa-box"></i> Sản phẩm
            </h6>
            <ul class="list-unstyled">
                ${orderDetail.items.map(item => `
                    <li class="mb-2">
                        <strong>Product ${item.productId}</strong> 
                        <span class="text-muted">x${item.quantity}</span>
                        <span class="float-end">₫${parseFloat(item.price).toLocaleString('vi-VN')}</span>
                    </li>
                `).join('')}
            </ul>
        </div>

        <div class="mb-4">
            <h6 class="border-bottom pb-2">
                <i class="fas fa-credit-card"></i> Thông tin thanh toán
            </h6>
            ${paymentHtml}
        </div>

        <div class="mb-4">
            <h6 class="border-bottom pb-2">
                <i class="fas fa-truck"></i> Thông tin vận chuyển
            </h6>
            ${shipmentHtml}
        </div>
    `;

    // Show the modal
    const modal = new bootstrap.Modal(modalElement);
    modal.show();
}

function createOrderDetailModal() {
    const modalHtml = `
        <div class="modal fade" id="orderDetailModal" tabindex="-1" aria-labelledby="orderDetailModalLabel" aria-hidden="true">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="orderDetailModalLabel">
                            <i class="fas fa-info-circle"></i> Chi tiết đơn hàng
                        </h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <!-- Content will be populated dynamically -->
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
                            <i class="fas fa-times"></i> Đóng
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `;
    document.body.insertAdjacentHTML('beforeend', modalHtml);
}

function getPaymentStatusColor(status) {
    switch (status) {
        case 'PENDING': return 'warning';
        case 'SUCCESS': return 'success';
        case 'FAILED': return 'danger';
        default: return 'secondary';
    }
}

function getPaymentStatusText(status) {
    switch (status) {
        case 'PENDING': return 'Đang chờ';
        case 'SUCCESS': return 'Thành công';
        case 'FAILED': return 'Thất bại';
        default: return status;
    }
}

function getShipmentStatusColor(status) {
    switch (status) {
        case 'CREATED': return 'secondary';
        case 'SHIPPING': return 'info';
        case 'COMPLETED': return 'success';
        case 'REJECTED': return 'danger';
        default: return 'secondary';
    }
}

function getShipmentStatusText(status) {
    switch (status) {
        case 'CREATED': return 'Đã tạo';
        case 'SHIPPING': return 'Đang giao';
        case 'COMPLETED': return 'Hoàn thành';
        case 'REJECTED': return 'Bị từ chối';
        default: return status;
    }
}

// Global functions for onclick handlers
window.showScreen = showScreen;
window.logout = logout;
window.showOrderDetail = showOrderDetail;
window.toggleAuthForm = toggleAuthForm;
window.togglePassword = togglePassword;

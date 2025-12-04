// app.js - Phiên bản sạch sẽ, tối ưu hoàn toàn
const state = {
    catPage: 0, catKeyword: '',
    prodPage: 0, prodKeyword: '', prodCatFilter: ''
};

// === UTILS ===
const $ = id => document.getElementById(id);
const showAlert = (id, type, msg) => {
    const el = $(id);
    if (!el) return;
    el.className = `alert alert-${type} show`;
    el.textContent = msg;
    setTimeout(() => el.classList.add('d-none'), 5000);
};

const resetForm = (formId, hiddenId, btnTextId) => {
    $(formId).reset();
    $(hiddenId).value = '';
    $(btnTextId).textContent = 'Tạo mới';
};

// === SCREEN ===
const adminLogin = () => {
    $('login-screen').classList.add('d-none');
    $('main-screen').classList.remove('d-none');
    showScreen('categories');
};

const logout = () => confirm('Đăng xuất quản trị?') && location.reload();

const showScreen = screen => {
    $('categories-screen').classList.toggle('d-none', screen !== 'categories');
    $('products-screen').classList.toggle('d-none', screen !== 'products');
    document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
    document.querySelector(`a[onclick="showScreen('${screen}')"]`).classList.add('active');

    if (screen === 'categories') loadCategories();
    if (screen === 'products') { loadProducts(); loadCategoryOptions(); }
};

// === PAGINATION (chuẩn, không lỗi) ===
const renderPagination = (id, total, current, onPageChange) => {
    const container = $(id);
    let html = '';
    if (current > 0) html += `<button class="pagination-btn" onclick="pageClick(${current-1})">Trước</button>`;
    for (let i = 0; i < total; i++) {
        html += `<button class="pagination-btn ${i===current?'active':''}" onclick="pageClick(${i})">${i+1}</button>`;
    }
    if (current < total-1) html += `<button class="pagination-btn" onclick="pageClick(${current+1})">Sau</button>`;
    container.innerHTML = html;

    // Closure để giữ callback
    window.pageClick = page => { onPageChange(page); };
};

// === CATEGORY ===
const loadCategories = async () => {
    try {
        const res = await AdminApiService.getCategories(state.catPage, 10, state.catKeyword);
        $('categories-table').querySelector('tbody').innerHTML = (res.content || []).map(c => `
            <tr>
                <td>${c.id}</td>
                <td>${c.name}</td>
                <td>${c.description || ''}</td>
                <td>
                    <button class="btn btn-sm btn-outline-primary" onclick="editCategory(${c.id})">Sửa</button>
                    <button class="btn btn-sm btn-outline-danger" onclick="deleteCategory(${c.id})">Xóa</button>
                </td>
            </tr>
        `).join('');
        renderPagination('category-pagination', res.totalPages || 1, state.catPage, p => {
            state.catPage = p; loadCategories();
        });
    } catch (e) {
        showAlert('category-alert', 'danger', 'Lỗi: ' + e.message);
    }
};

const editCategory = async id => {
    try {
        const c = await AdminApiService.getCategoryById(id);
        $('category-id').value = c.id;
        $('category-name').value = c.name;
        $('category-description').value = c.description || '';
        $('category-btn-text').textContent = 'Cập nhật';
    } catch {
        showAlert('category-alert', 'danger', 'Không tải được danh mục');
    }
};

const deleteCategory = async id => {
    if (!confirm('Xóa danh mục này?')) return;
    try {
        await AdminApiService.deleteCategory(id);
        showAlert('category-alert', 'success', 'Xóa thành công');
        loadCategories();
        loadCategoryOptions();
    } catch (e) {
        showAlert('category-alert', 'danger', e.message);
    }
};

$('category-form').onsubmit = async e => {
    e.preventDefault();
    const id = $('category-id').value;
    const data = {
        name: $('category-name').value.trim(),
        description: $('category-description').value.trim() || null
    };
    if (!data.name) return showAlert('category-alert', 'danger', 'Tên danh mục bắt buộc');

    try {
        id ? await AdminApiService.updateCategory(id, data)
            : await AdminApiService.createCategory(data);
        showAlert('category-alert', 'success', id ? 'Cập nhật thành công!' : 'Tạo thành công!');
        resetForm('category-form', 'category-id', 'category-btn-text');
        loadCategories();
        loadCategoryOptions();
    } catch (e) {
        showAlert('category-alert', 'danger', e.message);
    }
};

const searchCategories = () => {
    state.catKeyword = $('category-search').value.trim();
    state.catPage = 0;
    loadCategories();
};

// === PRODUCT ===
let currentProducts = [];

const loadProducts = async () => {
    try {
        const res = await AdminApiService.getProducts(state.prodPage, 10, state.prodKeyword, state.prodCatFilter);
        currentProducts = res.content || [];
        $('products-table').querySelector('tbody').innerHTML = currentProducts.map(p => `
            <tr>
                <td>${p.id}</td>
                <td>${p.name}</td>
                <td>${Number(p.price).toLocaleString('vi-VN')}₫</td>
                <td>${p.stockQuantity}</td>
                <td>${p.category?.name || '-'}</td>
                <td>
                    <button class="btn btn-sm btn-outline-primary" onclick="editProduct(${p.id})">Sửa</button>
                    <button class="btn btn-sm btn-outline-danger" onclick="deleteProduct(${p.id})">Xóa</button>
                </td>
            </tr>
        `).join('');
        renderPagination('product-pagination', res.totalPages || 1, state.prodPage, p => {
            state.prodPage = p; loadProducts();
        });
    } catch (e) {
        showAlert('product-alert', 'danger', 'Lỗi: ' + e.message);
    }
};

const editProduct = id => {
    const p = currentProducts.find(x => x.id === id);
    if (!p) return;
    $('product-id').value = p.id;
    $('product-name').value = p.name;
    $('product-description').value = p.description || '';
    $('product-price').value = p.price;
    $('product-stock').value = p.stockQuantity;
    $('product-category').value = p.category?.id || '';
    $('product-btn-text').textContent = 'Cập nhật';
};

const deleteProduct = async id => {
    if (!confirm('Xóa sản phẩm này?')) return;
    try {
        await AdminApiService.deleteProduct(id);
        showAlert('product-alert', 'success', 'Xóa thành công');
        loadProducts();
    } catch (e) {
        showAlert('product-alert', 'danger', e.message);
    }
};

$('product-form').onsubmit = async e => {
    e.preventDefault();
    const id = $('product-id').value;
    const data = {
        name: $('product-name').value.trim(),
        description: $('product-description').value.trim() || null,
        price: +$('product-price').value,
        stockQuantity: +$('product-stock').value,
        categoryId: +$('product-category').value
    };

    if (!data.name || !data.description || data.price <= 0 || data.stockQuantity < 0 || !data.categoryId) {
        return showAlert('product-alert', 'danger', 'Vui lòng điền đầy đủ và hợp lệ');
    }

    try {
        id ? await AdminApiService.updateProduct(id, data)
            : await AdminApiService.createProduct(data);
        showAlert('product-alert', 'success', id ? 'Cập nhật thành công!' : 'Tạo thành công!');
        resetForm('product-form', 'product-id', 'product-btn-text');
        loadProducts();
    } catch (e) {
        showAlert('product-alert', 'danger', e.message);
    }
};

const searchProducts = () => {
    state.prodKeyword = $('product-search').value.trim();
    state.prodCatFilter = $('product-category-filter').value;
    state.prodPage = 0;
    loadProducts();
};

const loadCategoryOptions = async () => {
    try {
        const res = await AdminApiService.getCategories(0, 200);
        const opts = res.content.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
        $('product-category').innerHTML = '<option value="">Chọn danh mục</option>' + opts;
        $('product-category-filter').innerHTML = '<option value="">Tất cả danh mục</option>' + opts;
    } catch (e) {
        console.error(e);
    }
};

// === EXPOSE GLOBALS ===
Object.assign(window, {
    adminLogin, logout, showScreen,
    searchCategories, editCategory, deleteCategory, resetForm,
    searchProducts, editProduct, deleteProduct,
    loadCategoryOptions
});
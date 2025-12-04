// api.js
const API_BASE = 'http://localhost:8081';

class AdminApiService {
    static async request(url, options = {}) {
        const res = await fetch(API_BASE + url, {
            ...options,
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            credentials: 'include'
        });

        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || 'Lỗi server');
        }
        return res.json();
    }

    // === CATEGORY ===
    static async getCategories(page = 0, size = 10, keyword = '') {
        const params = new URLSearchParams({ page, size, keyword });
        return this.request(`/api/categories?${params}`);
    }
    static async createCategory(data) { return this.request('/api/categories', { method: 'POST', body: JSON.stringify(data) }); }
    static async updateCategory(id, data) { return this.request(`/api/categories/${id}`, { method: 'PUT', body: JSON.stringify(data) }); }
    static async deleteCategory(id) { return this.request(`/api/categories/${id}`, { method: 'DELETE' }); }
    static async getCategoryById(id) { return this.request(`/api/categories/${id}`); } // ĐÃ THÊM

    // === PRODUCT ===
    static async getProducts(page = 0, size = 10, keyword = '', categoryId = '') {
        const params = new URLSearchParams({ page, size, keyword });
        if (categoryId) params.append('categoryId', categoryId);
        return this.request(`/api/products?${params}`);
    }
    static async createProduct(data) { return this.request('/api/products', { method: 'POST', body: JSON.stringify(data) }); }
    static async updateProduct(id, data) { return this.request(`/api/products/${id}`, { method: 'PUT', body: JSON.stringify(data) }); }
    static async deleteProduct(id) { return this.request(`/api/products/${id}`, { method: 'DELETE' }); }
}

window.AdminApiService = AdminApiService;
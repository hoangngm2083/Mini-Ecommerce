// API Configuration
const API_CONFIG = {
    BASE_URL: 'http://localhost:8081', // Change this to your backend URL
    ENDPOINTS: {
        PRODUCTS: '/api/products',
        ORDERS: '/api/orders',
        AUTH: '/api/auth'
    }
};

// Global user state (simulated authentication)
let currentUser = null;

// API Utility Functions
class ApiService {
    static async makeRequest(url, options = {}) {
        const fullUrl = API_CONFIG.BASE_URL + url;

        // Merge headers properly
        const defaultHeaders = {
            'Content-Type': 'application/json'
        };

        const finalHeaders = { ...defaultHeaders, ...options.headers };

        const finalOptions = {
            ...options,
            headers: finalHeaders
        };

        try {
            const response = await fetch(fullUrl, finalOptions);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`);
            }

            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    }

    // Product APIs
    static async searchProducts(keyword = '', page = 0, size = 10) {
        const params = new URLSearchParams({
            page: page.toString(),
            size: size.toString()
        });

        if (keyword.trim()) {
            params.append('keyword', keyword.trim());
        }

        return await this.makeRequest(`${API_CONFIG.ENDPOINTS.PRODUCTS}?${params}`);
    }

    static async getProduct(productId) {
        return await this.makeRequest(`${API_CONFIG.ENDPOINTS.PRODUCTS}/${productId}`);
    }

    // Order APIs
    static async createOrder(orderPayload) {
        if (!currentUser || !currentUser.id) {
            throw new Error('User not authenticated');
        }

        return await this.makeRequest(API_CONFIG.ENDPOINTS.ORDERS, {
            method: 'POST',
            headers: {
                'userId': currentUser.id.toString()
            },
            body: JSON.stringify(orderPayload)
        });
    }

    static async getUserOrders() {
        if (!currentUser || !currentUser.id) {
            throw new Error('User not authenticated');
        }

        return await this.makeRequest(`${API_CONFIG.ENDPOINTS.ORDERS}/me`, {
            headers: {
                'userId': currentUser.id.toString()
            }
        });
    }

    static async getOrderDetail(orderId) {
        if (!currentUser || !currentUser.id) {
            throw new Error('User not authenticated');
        }

        return await this.makeRequest(`${API_CONFIG.ENDPOINTS.ORDERS}/${orderId}`, {
            headers: {
                'userId': currentUser.id.toString()
            }
        });
    }

    // Authentication APIs (simulated)
    static async login(credentials) {
        // Simulated login - in real app, this would call actual API
        const response = await this.makeRequest(API_CONFIG.ENDPOINTS.AUTH + '/login', {
            method: 'POST',
            body: JSON.stringify(credentials)
        });

        currentUser = response.user;
        return response;
    }

    static async register(userData) {
        // Simulated register - in real app, this would call actual API
        const response = await this.makeRequest(API_CONFIG.ENDPOINTS.AUTH + '/register', {
            method: 'POST',
            body: JSON.stringify(userData)
        });

        currentUser = response.user;
        return response;
    }

    // User management
    static setCurrentUser(user) {
        currentUser = user;
    }

    static getCurrentUser() {
        return currentUser;
    }

    static logout() {
        currentUser = null;
    }

    static isAuthenticated() {
        return currentUser !== null;
    }
}

// Export for use in other files
window.ApiService = ApiService;

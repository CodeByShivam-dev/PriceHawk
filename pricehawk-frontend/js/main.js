// js/main.js - LOGOS + BACKEND READY
function getStoreLogo(store) {
    const logos = {
        'amazon': 'assets/images/amazon.png',
        'flipkart': 'assets/images/flipkart.png',
        'croma': 'assets/images/croma.png'
    };
    return logos[store.toLowerCase()] || 'assets/images/default.png';
}

function searchPhone() {
    let query = document.getElementById("phoneSearch").value.trim();
    if(!query) return alert('Enter phone name');

    const results = document.getElementById("results");
    results.innerHTML = `<div class="loading"><div class="spinner"></div><p>🔍 Searching Results...</p></div>`;

    fetch(`http://localhost:8080/api/smartphones?query=${encodeURIComponent(query)}`)
    .then(response => response.json())
    .then(data => {
        if(!data || data.length === 0) {
            results.innerHTML = `<div class="empty-state"><div class="empty-icon">📱</div><p>No prices found for "${query}"</p></div>`;
            // 🔥 AI VERDICT BOX UPDATE (YE ADD KARO 👇)
fetch(`http://localhost:8080/api/ai-verdict?query=${encodeURIComponent(query)}`)
.then(res => res.json())
.then(aiData => {
    document.getElementById('aiMain').textContent = aiData.main || '📱 Premium Flagship';
    document.getElementById('aiSub').textContent = aiData.sub || `${query} Analysis`;
})
.catch(() => {
    document.getElementById('aiMain').textContent = '📱 6.7″ AMOLED 120Hz';
    document.getElementById('aiSub').textContent = `${query} - Live Specs Ready`;
});

            return;
        }
        
        // ✅ LOGOS + REAL DATA
        results.innerHTML = data.map(item => `
    <div class="result-card">
        <div class="store-logo">
            <img src="${getStoreLogo(item.store)}" alt="${item.store}">
        </div>
        <h4>${item.store || 'Store'}</h4>
        <div class="price">
            ${item.price ? '₹'+item.price.toLocaleString('en-IN') : 'Live price loading...'}
        </div>
        <button class="view-btn" 
            onclick="window.open('${item.productUrl || '#'}','_blank')">
            View Deal
        </button>
    </div>
`).join('');
    })
    .catch(error => {
        results.innerHTML = `<div class="empty-state"><div class="empty-icon">⚠️</div><p>Backend localhost:8080 OFF!</p></div>`;
        console.error('Backend error:', error);
    });
}

// Enter key support
document.addEventListener('DOMContentLoaded', function() {
    document.getElementById("phoneSearch").addEventListener("keypress", function(e) {
        if(e.key === "Enter") searchPhone();
    });
    
    // Search button click
    document.querySelector('.btn').addEventListener('click', searchPhone);
});

// =============================================================================
// BANKAI — Digital Gold Wallet  |  k6 Load Test Script
// =============================================================================
// Covers ALL API endpoints across 8 controllers with 50+ virtual users.
//
// Usage:
//   k6 run loadtest.js
//   k6 run --vus 100 --duration 5m loadtest.js   (override defaults)
//   k6 run --out json=results.json loadtest.js    (export results)
//
// Environment Variables:
//   BASE_URL          — Backend base URL   (default: http://localhost:8081)
//   USER_EMAIL        — Pre-existing user  (default: auto-registers)
//   USER_PASSWORD     — Pre-existing pwd   (default: auto-registers)
//   VENDOR_EMAIL      — Pre-existing vendor (default: auto-registers)
//   VENDOR_PASSWORD   — Pre-existing pwd   (default: auto-registers)
// =============================================================================

import http from 'k6/http';
import { check, group, sleep, fail } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

// ── Custom Metrics ──────────────────────────────────────────────────────────────
const errorRate       = new Rate('errors');
const loginDuration   = new Trend('login_duration', true);
const registerDur     = new Trend('register_duration', true);
const dashboardDur    = new Trend('dashboard_duration', true);
const goldPriceDur    = new Trend('gold_price_duration', true);
const buyGoldDur      = new Trend('buy_gold_duration', true);
const sellGoldDur     = new Trend('sell_gold_duration', true);
const walletTopupDur  = new Trend('wallet_topup_duration', true);
const apiCalls        = new Counter('total_api_calls');

// ── Configuration ───────────────────────────────────────────────────────────────
const BASE_URL = __ENV.BASE_URL || 'http://65.2.183.244:8081/api';

export const options = {
  stages: [
    { duration: '30s', target: 20 },   // Ramp up to 20 users
    { duration: '1m',  target: 50 },   // Ramp up to 50 users
    { duration: '2m',  target: 55 },   // Stay at 55 users (sustained load)
    { duration: '1m',  target: 75 },   // Spike to 75 users
    { duration: '30s', target: 50 },   // Back down to 50
    { duration: '30s', target: 0 },    // Ramp down to 0
  ],
  thresholds: {
    http_req_duration:   ['p(95)<3000', 'p(99)<5000'],   // 95th < 3s, 99th < 5s
    http_req_failed:     ['rate<0.10'],                  // <10% failure rate
    errors:              ['rate<0.15'],                   // Custom error rate
    login_duration:      ['p(95)<2000'],
    dashboard_duration:  ['p(95)<3000'],
    gold_price_duration: ['p(95)<1500'],
    buy_gold_duration:   ['p(95)<3000'],
    wallet_topup_duration: ['p(95)<2500'],
  },
};

// ── Helpers ─────────────────────────────────────────────────────────────────────

function jsonHeaders(token) {
  const h = { 'Content-Type': 'application/json' };
  if (token) h['Authorization'] = `Bearer ${token}`;
  return { headers: h };
}

function uniqueId() {
  return `${Date.now()}_${Math.random().toString(36).substring(2, 8)}`;
}

function randomBigDecimal(min, max, decimals = 5) {
  return (Math.random() * (max - min) + min).toFixed(decimals);
}

function randomPhone() {
  return String(Math.floor(7000000000 + Math.random() * 2999999999));
}

function checkResponse(res, name, expectedStatus = 200) {
  const passed = check(res, {
    [`${name} — status ${expectedStatus}`]: (r) => r.status === expectedStatus,
    [`${name} — response time < 5s`]:       (r) => r.timings.duration < 5000,
  });
  errorRate.add(!passed);
  apiCalls.add(1);
  return passed;
}

// =============================================================================
//  SETUP — Runs once before load test. Registers a user + vendor to share.
// =============================================================================
export function setup() {
  const uid = uniqueId();
  const userData = {
    userEmail: __ENV.USER_EMAIL || 'pradeep.kumar@example.in',
    userPassword: __ENV.USER_PASSWORD || 'User@123',
    vendorEmail: __ENV.VENDOR_EMAIL || 'rohit.sona@example.com',
    vendorPassword: __ENV.VENDOR_PASSWORD || 'Vendor@123',
  };

  // ── Register a test user ──────────────────────────────────────────────────
  if (!__ENV.USER_EMAIL) {
    const regRes = http.post(`${BASE_URL}/user/auth/register`, JSON.stringify({
      name: `Load Test User ${uid}`,
      email: userData.userEmail,
      password: userData.userPassword,
      street: '123 Load Test Street',
      city: 'Mumbai',
      state: 'Maharashtra',
      postalCode: '400001',
      country: 'India',
    }), jsonHeaders());

    if (regRes.status !== 201) {
      console.warn(`User registration failed (${regRes.status}): ${regRes.body}`);
    } else {
      const body = JSON.parse(regRes.body);
      userData.userId = body.userId || body.user_id;
      userData.userToken = body.token;
      console.log(`✓ Setup user registered: ${userData.userEmail} (id=${userData.userId})`);
    }
  }

  // ── Login user (or re-login if pre-existing) ─────────────────────────────
  if (!userData.userToken) {
    const loginRes = http.post(`${BASE_URL}/user/auth/login`, JSON.stringify({
      email: userData.userEmail,
      password: userData.userPassword,
    }), jsonHeaders());

    if (loginRes.status === 200) {
      const body = JSON.parse(loginRes.body);
      userData.userId = body.userId || body.user_id;
      userData.userToken = body.token;
    } else {
      console.error(`Setup user login failed: ${loginRes.status} ${loginRes.body}`);
    }
  }

  // ── Register a test vendor ────────────────────────────────────────────────
  if (!__ENV.VENDOR_EMAIL) {
    const vRegRes = http.post(`${BASE_URL}/vendor/auth/register`, JSON.stringify({
      vendorName: `Load Vendor ${uid}`,
      contactPersonName: `Load Vendor Contact ${uid}`,
      contactEmail: userData.vendorEmail,
      contactPhone: randomPhone(),
      password: userData.vendorPassword,
      street: '456 Vendor Avenue',
      city: 'Delhi',
      state: 'Delhi',
      postalCode: '110001',
      country: 'India',
      description: 'Load testing vendor',
      websiteUrl: 'https://loadtest-vendor.example.com',
    }), jsonHeaders());

    if (vRegRes.status !== 201) {
      console.warn(`Vendor registration failed (${vRegRes.status}): ${vRegRes.body}`);
    } else {
      const body = JSON.parse(vRegRes.body);
      userData.vendorId = body.userId || body.user_id;
      userData.vendorToken = body.token;
      console.log(`✓ Setup vendor registered: ${userData.vendorEmail} (id=${userData.vendorId})`);
    }
  }

  // ── Login vendor (or re-login if pre-existing) ────────────────────────────
  if (!userData.vendorToken) {
    const vLoginRes = http.post(`${BASE_URL}/vendor/auth/login`, JSON.stringify({
      email: userData.vendorEmail,
      password: userData.vendorPassword,
    }), jsonHeaders());

    if (vLoginRes.status === 200) {
      const body = JSON.parse(vLoginRes.body);
      userData.vendorId = body.userId || body.user_id;
      userData.vendorToken = body.token;
    } else {
      console.error(`Setup vendor login failed: ${vLoginRes.status} ${vLoginRes.body}`);
    }
  }

  // ── Topup the user wallet so gold operations work ─────────────────────────
  if (userData.userToken && userData.userId) {
    http.post(`${BASE_URL}/wallet/topup`, JSON.stringify({
      user_id: userData.userId,
      amount: 500000,
      payment_method: 'UPI',
    }), jsonHeaders(userData.userToken));
    console.log(`✓ Setup wallet topped up for user ${userData.userId}`);
  }

  return userData;
}

// =============================================================================
//  DEFAULT — Main VU iteration (each VU runs this repeatedly)
// =============================================================================
export default function (data) {

  // ╔═══════════════════════════════════════════════════════════════════════════╗
  // ║  1. USER AUTH — Register + Login                                        ║
  // ╚═══════════════════════════════════════════════════════════════════════════╝
  let userToken = null;
  let userId    = null;

  group('01 — User Auth', () => {

    // Register a unique user per iteration
    const uid = uniqueId();
    const email = `k6user_${__VU}_${uid}@test.com`;
    const password = 'K6TestPass1';

    const regRes = http.post(`${BASE_URL}/user/auth/register`, JSON.stringify({
      name: `K6 User ${__VU}`,
      email: email,
      password: password,
      street: `${__VU} Test Lane`,
      city: 'Bangalore',
      state: 'Karnataka',
      postalCode: '560001',
      country: 'India',
    }), jsonHeaders());

    registerDur.add(regRes.timings.duration);
    // Could be 201 or 409 (duplicate) — both are acceptable
    const regOk = check(regRes, {
      'User register — status 201 or 409': (r) => r.status === 201 || r.status === 409,
    });
    errorRate.add(!regOk);
    apiCalls.add(1);

    if (regRes.status === 201) {
      const body = JSON.parse(regRes.body);
      userToken = body.token;
      userId = body.userId || body.user_id;
    }

    // Login
    const loginRes = http.post(`${BASE_URL}/user/auth/login`, JSON.stringify({
      email: regRes.status === 201 ? email : data.userEmail,
      password: regRes.status === 201 ? password : data.userPassword,
    }), jsonHeaders());

    loginDuration.add(loginRes.timings.duration);
    checkResponse(loginRes, 'User login', 200);

    if (loginRes.status === 200) {
      const body = JSON.parse(loginRes.body);
      userToken = body.token;
      userId = body.userId || body.user_id;
    }
  });

  // Fallback to setup data
  if (!userToken) { userToken = data.userToken; userId = data.userId; }
  if (!userToken) { console.warn('No user token, skipping authenticated tests'); return; }

  sleep(0.5);

  // ╔═══════════════════════════════════════════════════════════════════════════╗
  // ║  2. GOLD PRICE — Public-ish endpoints (authenticated in this app)       ║
  // ╚═══════════════════════════════════════════════════════════════════════════╝
  group('02 — Gold Price', () => {

    // GET /gold/price
    const priceRes = http.get(`${BASE_URL}/gold/price`, jsonHeaders(userToken));
    goldPriceDur.add(priceRes.timings.duration);
    checkResponse(priceRes, 'Get gold price');

    // GET /gold/price-history?days=30
    const histRes = http.get(`${BASE_URL}/gold/price-history?days=30`, jsonHeaders(userToken));
    checkResponse(histRes, 'Get price history (30d)');

    // GET /gold/price-history?days=7
    const hist7Res = http.get(`${BASE_URL}/gold/price-history?days=7`, jsonHeaders(userToken));
    checkResponse(hist7Res, 'Get price history (7d)');

    // GET /gold/price-history?days=90
    const hist90Res = http.get(`${BASE_URL}/gold/price-history?days=90`, jsonHeaders(userToken));
    checkResponse(hist90Res, 'Get price history (90d)');
  });

  sleep(0.3);

  // ╔═══════════════════════════════════════════════════════════════════════════╗
  // ║  3. USER DASHBOARD                                                      ║
  // ╚═══════════════════════════════════════════════════════════════════════════╝
  group('03 — User Dashboard', () => {
    if (!userId) return;

    // GET /{id}/dashboard
    const dashRes = http.get(`${BASE_URL}/users/${userId}/dashboard`, jsonHeaders(userToken));
    dashboardDur.add(dashRes.timings.duration);
    checkResponse(dashRes, 'User dashboard');

    // GET /{id}/holdings
    const holdRes = http.get(`${BASE_URL}/users/${userId}/holdings`, jsonHeaders(userToken));
    checkResponse(holdRes, 'User holdings');

    // GET /{id}/transactions
    const txnRes = http.get(`${BASE_URL}/users/${userId}/transactions`, jsonHeaders(userToken));
    checkResponse(txnRes, 'User transactions');

    // GET /{id}/addresses
    const addrRes = http.get(`${BASE_URL}/users/${userId}/addresses`, jsonHeaders(userToken));
    checkResponse(addrRes, 'User addresses');

    // GET /{id}/physical-gold
    const pgRes = http.get(`${BASE_URL}/users/${userId}/physical-gold`, jsonHeaders(userToken));
    checkResponse(pgRes, 'User physical gold');

    // GET /{id}/payments
    const payRes = http.get(`${BASE_URL}/users/${userId}/payments`, jsonHeaders(userToken));
    checkResponse(payRes, 'User payments');
  });

  sleep(0.3);

  // ╔═══════════════════════════════════════════════════════════════════════════╗
  // ║  4. WALLET TOPUP                                                        ║
  // ╚═══════════════════════════════════════════════════════════════════════════╝
  group('04 — Wallet Topup', () => {
    if (!userId) return;

    const topupRes = http.post(`${BASE_URL}/wallet/topup`, JSON.stringify({
      user_id: userId,
      amount: parseFloat(randomBigDecimal(100, 10000, 2)),
      payment_method: ['UPI', 'CARD', 'NET_BANKING'][Math.floor(Math.random() * 3)],
    }), jsonHeaders(userToken));

    walletTopupDur.add(topupRes.timings.duration);
    checkResponse(topupRes, 'Wallet topup');
  });

  sleep(0.3);

  // ╔═══════════════════════════════════════════════════════════════════════════╗
  // ║  5. VENDORS LIST (accessible by USER and VENDOR roles)                  ║
  // ╚═══════════════════════════════════════════════════════════════════════════╝
  let vendorId = data.vendorId;

  group('05 — Vendors List', () => {

    const vendorsRes = http.get(`${BASE_URL}/vendors`, jsonHeaders(userToken));
    checkResponse(vendorsRes, 'List vendors');

    // Grab a vendor ID from the list if we don't have one from setup
    if (vendorsRes.status === 200) {
      try {
        const vendors = JSON.parse(vendorsRes.body);
        if (Array.isArray(vendors) && vendors.length > 0) {
          vendorId = vendorId || vendors[0].vendorId || vendors[0].vendor_id || vendors[0].id;
        }
      } catch (e) { /* ignore parse errors */ }
    }
  });

  sleep(0.3);

  // ╔═══════════════════════════════════════════════════════════════════════════╗
  // ║  6. VIRTUAL GOLD — Buy & Sell                                           ║
  // ╚═══════════════════════════════════════════════════════════════════════════╝
  let holdingId = null;

  group('06 — Virtual Gold', () => {
    if (!userId || !vendorId) return;

    // Buy virtual gold
    const buyQty = randomBigDecimal(0.001, 0.1, 5);
    const buyRes = http.post(`${BASE_URL}/virtual-gold/buy`, JSON.stringify({
      user_id: userId,
      vendor_id: vendorId,
      quantity: parseFloat(buyQty),
    }), jsonHeaders(userToken));

    buyGoldDur.add(buyRes.timings.duration);
    const buyOk = checkResponse(buyRes, 'Buy virtual gold');

    // Extract holding ID for sell
    if (buyOk && buyRes.status === 200) {
      try {
        const body = JSON.parse(buyRes.body);
        holdingId = body.holdingId || body.holding_id || body.id;
      } catch (e) { /* ignore */ }
    }

    sleep(0.2);

    // Sell virtual gold (only if we have a holding)
    if (holdingId) {
      const sellQty = randomBigDecimal(0.0001, parseFloat(buyQty) * 0.5, 5);
      const sellRes = http.post(`${BASE_URL}/virtual-gold/sell`, JSON.stringify({
        user_id: userId,
        holding_id: holdingId,
        quantity: parseFloat(sellQty),
      }), jsonHeaders(userToken));

      sellGoldDur.add(sellRes.timings.duration);
      checkResponse(sellRes, 'Sell virtual gold');
    }
  });

  sleep(0.3);

  // ╔═══════════════════════════════════════════════════════════════════════════╗
  // ║  7. PHYSICAL GOLD — Buy & Convert                                       ║
  // ╚═══════════════════════════════════════════════════════════════════════════╝
  group('07 — Physical Gold', () => {
    if (!userId || !vendorId) return;

    // Get user addresses first to find a delivery address
    const addrRes = http.get(`${BASE_URL}/users/${userId}/addresses`, jsonHeaders(userToken));
    let addressId = null;
    if (addrRes.status === 200) {
      try {
        const addrs = JSON.parse(addrRes.body);
        if (Array.isArray(addrs) && addrs.length > 0) {
          addressId = addrs[0].addressId || addrs[0].address_id || addrs[0].id;
        }
      } catch (e) { /* ignore */ }
    }

    if (!addressId) return; // Can't test without an address

    // Buy physical gold
    const buyPRes = http.post(`${BASE_URL}/physical-gold/buy`, JSON.stringify({
      user_id: userId,
      vendor_id: vendorId,
      quantity: parseFloat(randomBigDecimal(0.5, 2, 3)),
      delivery_address_id: addressId,
    }), jsonHeaders(userToken));

    checkResponse(buyPRes, 'Buy physical gold');

    sleep(0.2);

    // Convert virtual → physical (only if we have a holding)
    if (holdingId) {
      const convRes = http.post(`${BASE_URL}/physical-gold/convert`, JSON.stringify({
        user_id: userId,
        holding_id: holdingId,
        quantity: parseFloat(randomBigDecimal(0.0001, 0.01, 5)),
        delivery_address_id: addressId,
      }), jsonHeaders(userToken));

      checkResponse(convRes, 'Convert to physical gold');
    }
  });

  sleep(0.3);

  // ╔═══════════════════════════════════════════════════════════════════════════╗
  // ║  8. USER PROFILE UPDATE                                                 ║
  // ╚═══════════════════════════════════════════════════════════════════════════╝
  group('08 — User Profile Update', () => {
    if (!userId) return;

    // Get current dashboard to read current email
    const dashRes = http.get(`${BASE_URL}/users/${userId}/dashboard`, jsonHeaders(userToken));
    let currentEmail = null;
    if (dashRes.status === 200) {
      try {
        const body = JSON.parse(dashRes.body);
        currentEmail = body.email;
      } catch (e) { /* ignore */ }
    }

    if (!currentEmail) return;

    const updateRes = http.put(`${BASE_URL}/users/${userId}/profile`, JSON.stringify({
      name: `K6 Updated User ${__VU}`,
      email: currentEmail, // Keep same email to avoid conflicts
      street: `${__VU} Updated Lane`,
      city: 'Hyderabad',
      state: 'Telangana',
      postalCode: '500001',
      country: 'India',
    }), jsonHeaders(userToken));

    checkResponse(updateRes, 'Update user profile');

    // Use new token if provided
    const newToken = updateRes.headers['X-New-Token'];
    if (newToken) userToken = newToken;
  });

  sleep(0.3);

  // ╔═══════════════════════════════════════════════════════════════════════════╗
  // ║  9. VENDOR AUTH — Register + Login                                      ║
  // ╚═══════════════════════════════════════════════════════════════════════════╝
  let vendorToken = null;
  let testVendorId = null;

  group('09 — Vendor Auth', () => {

    const uid = uniqueId();
    const email = `k6vendor_${__VU}_${uid}@test.com`;
    const password = 'K6VendorPass1';

    // Register vendor
    const regRes = http.post(`${BASE_URL}/vendor/auth/register`, JSON.stringify({
      vendorName: `K6 Vendor ${__VU}_${uid}`,
      contactPersonName: `K6 Contact ${__VU}`,
      contactEmail: email,
      contactPhone: randomPhone(),
      password: password,
      street: `${__VU} Vendor Road`,
      city: 'Chennai',
      state: 'Tamil Nadu',
      postalCode: '600001',
      country: 'India',
      description: 'K6 load test vendor',
      websiteUrl: 'https://k6vendor.example.com',
    }), jsonHeaders());

    registerDur.add(regRes.timings.duration);
    const regOk = check(regRes, {
      'Vendor register — status 201 or 409': (r) => r.status === 201 || r.status === 409,
    });
    errorRate.add(!regOk);
    apiCalls.add(1);

    if (regRes.status === 201) {
      const body = JSON.parse(regRes.body);
      vendorToken = body.token;
      testVendorId = body.userId || body.user_id;
    }

    // Login vendor
    const loginRes = http.post(`${BASE_URL}/vendor/auth/login`, JSON.stringify({
      email: regRes.status === 201 ? email : data.vendorEmail,
      password: regRes.status === 201 ? password : data.vendorPassword,
    }), jsonHeaders());

    loginDuration.add(loginRes.timings.duration);
    checkResponse(loginRes, 'Vendor login', 200);

    if (loginRes.status === 200) {
      const body = JSON.parse(loginRes.body);
      vendorToken = body.token;
      testVendorId = body.userId || body.user_id;
    }
  });

  // Fallback
  if (!vendorToken) { vendorToken = data.vendorToken; testVendorId = data.vendorId; }
  if (!vendorToken || !testVendorId) return;

  sleep(0.3);

  // ╔═══════════════════════════════════════════════════════════════════════════╗
  // ║  10. VENDOR DASHBOARD                                                   ║
  // ╚═══════════════════════════════════════════════════════════════════════════╝
  group('10 — Vendor Dashboard', () => {

    // GET /{id}/dashboard
    const dashRes = http.get(`${BASE_URL}/vendors/${testVendorId}/dashboard`, jsonHeaders(vendorToken));
    dashboardDur.add(dashRes.timings.duration);
    checkResponse(dashRes, 'Vendor dashboard');

    // GET /{id}/branches
    const branchRes = http.get(`${BASE_URL}/vendors/${testVendorId}/branches`, jsonHeaders(vendorToken));
    checkResponse(branchRes, 'Vendor branches');

    // GET /{id}/transactions
    const txnRes = http.get(`${BASE_URL}/vendors/${testVendorId}/transactions`, jsonHeaders(vendorToken));
    checkResponse(txnRes, 'Vendor transactions');
  });

  sleep(0.3);

  // ╔═══════════════════════════════════════════════════════════════════════════╗
  // ║  11. VENDOR BRANCH MANAGEMENT — Add + Delete                            ║
  // ╚═══════════════════════════════════════════════════════════════════════════╝
  group('11 — Vendor Branch Management', () => {

    // Add a branch
    const addRes = http.post(`${BASE_URL}/vendors/${testVendorId}/branches`, JSON.stringify({
      street: `${__VU} Branch Street ${Date.now()}`,
      city: 'Pune',
      state: 'Maharashtra',
      postal_code: '411001',
      country: 'India',
      initial_quantity: parseFloat(randomBigDecimal(0, 10, 3)),
    }), jsonHeaders(vendorToken));

    checkResponse(addRes, 'Add vendor branch', 201);

    let branchId = null;
    if (addRes.status === 201) {
      try {
        const body = JSON.parse(addRes.body);
        branchId = body.branchId || body.branch_id || body.id;
      } catch (e) { /* ignore */ }
    }

    sleep(0.2);

    // Delete the branch we just created
    if (branchId) {
      const delRes = http.del(
        `${BASE_URL}/vendors/${testVendorId}/branches/${branchId}`,
        null,
        jsonHeaders(vendorToken)
      );
      checkResponse(delRes, 'Delete vendor branch', 204);
    }
  });

  sleep(0.3);

  // ╔═══════════════════════════════════════════════════════════════════════════╗
  // ║  12. VENDOR ADD GOLD                                                    ║
  // ╚═══════════════════════════════════════════════════════════════════════════╝
  group('12 — Vendor Add Gold', () => {

    // First, get branches to find one
    const branchRes = http.get(`${BASE_URL}/vendors/${testVendorId}/branches`, jsonHeaders(vendorToken));
    let targetBranchId = null;
    if (branchRes.status === 200) {
      try {
        const branches = JSON.parse(branchRes.body);
        if (Array.isArray(branches) && branches.length > 0) {
          targetBranchId = branches[0].branchId || branches[0].branch_id || branches[0].id;
        }
      } catch (e) { /* ignore */ }
    }

    if (!targetBranchId) return;

    const addGoldRes = http.post(`${BASE_URL}/vendors/${testVendorId}/add-gold`, JSON.stringify({
      branchId: targetBranchId,
      quantity: parseFloat(randomBigDecimal(1, 50, 3)),
    }), jsonHeaders(vendorToken));

    checkResponse(addGoldRes, 'Vendor add gold');
  });

  sleep(0.3);

  // ╔═══════════════════════════════════════════════════════════════════════════╗
  // ║  13. VENDOR PROFILE UPDATE                                              ║
  // ╚═══════════════════════════════════════════════════════════════════════════╝
  group('13 — Vendor Profile Update', () => {

    // Get current dashboard to read current email
    const dashRes = http.get(`${BASE_URL}/vendors/${testVendorId}/dashboard`, jsonHeaders(vendorToken));
    let currentEmail = null;
    if (dashRes.status === 200) {
      try {
        const body = JSON.parse(dashRes.body);
        currentEmail = body.contactEmail || body.contact_email;
      } catch (e) { /* ignore */ }
    }

    if (!currentEmail) return;

    const updateRes = http.put(`${BASE_URL}/vendors/${testVendorId}/profile`, JSON.stringify({
      contactPersonName: `K6 Updated Vendor Contact ${__VU}`,
      contactEmail: currentEmail, // Keep same to avoid conflicts
      contactPhone: randomPhone(),
      description: `Updated by k6 VU ${__VU} at ${new Date().toISOString()}`,
      websiteUrl: 'https://k6vendor-updated.example.com',
    }), jsonHeaders(vendorToken));

    checkResponse(updateRes, 'Update vendor profile');

    const newToken = updateRes.headers['X-New-Token'];
    if (newToken) vendorToken = newToken;
  });

  sleep(0.5);

  // ╔═══════════════════════════════════════════════════════════════════════════╗
  // ║  14. DASHBOARD REFRESH (simulate user polling dashboard)                ║
  // ╚═══════════════════════════════════════════════════════════════════════════╝
  group('14 — Dashboard Refresh Cycle', () => {
    if (!userId) return;

    // Simulate a user refreshing their dashboard multiple times
    for (let i = 0; i < 3; i++) {
      const res = http.get(`${BASE_URL}/users/${userId}/dashboard`, jsonHeaders(userToken));
      dashboardDur.add(res.timings.duration);
      checkResponse(res, `Dashboard refresh ${i + 1}`);
      sleep(0.2);
    }
  });

  sleep(1);
}

// =============================================================================
//  TEARDOWN — Runs once after all VUs finish
// =============================================================================
export function teardown(data) {
  console.log('════════════════════════════════════════════════════════════════');
  console.log(' BANKAI Load Test Complete');
  console.log(`  User:   ${data.userEmail}  (id=${data.userId})`);
  console.log(`  Vendor: ${data.vendorEmail} (id=${data.vendorId})`);
  console.log('════════════════════════════════════════════════════════════════');
}

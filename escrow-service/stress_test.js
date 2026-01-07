import http from 'k6/http';
import { check, sleep } from 'k6';

/**
 * STRESS TEST CONFIGURATION
 * * Stages:
 * 1. Warm up: Ramp up to 10 users over 30 seconds.
 * 2. High Load: Stay at 50 concurrent users for 1 minute (Heavy GC pressure).
 * 3. Cool down: Ramp down to 0 over 30 seconds.
 */
export const options = {
    stages: [
        { duration: '30s', target: 10 },
        { duration: '1m', target: 50 },
        { duration: '30s', target: 0 },
    ],
    // Success Criteria: 95% of requests must finish within 500ms
    thresholds: {
        http_req_duration: ['p(95)<500'],
        http_req_failed: ['rate<0.01'], // Less than 1% failure
    },
};

const BASE_URL = 'http://localhost:8080/api/v1';

// Pass your JWT Token via environment variable:
// k6 run -e TOKEN=ey... stress_test.js
const TOKEN = __ENV.TOKEN;

export default function () {
    if (!TOKEN) {
        console.error('Error: TOKEN environment variable is required.');
        // Fail the iteration if no token
        throw new Error('Please provide a valid JWT token via -e TOKEN=...');
    }

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${TOKEN}`,
        },
    };

    // 1. Fetch Transaction History (Read-Heavy Operation)
    // This stresses DB mapping, Hibernate caching, and JSON serialization (Jackson).
    const res = http.get(`${BASE_URL}/transactions`, params);

    // 2. Validate Response
    check(res, {
        'status is 200': (r) => r.status === 200,
        'content type is json': (r) => r.headers['Content-Type'].includes('application/json'),
    });

    // 3. Pause briefly (0.5s to 1s) to simulate real user pacing
    sleep(Math.random() * 0.5 + 0.5);
}
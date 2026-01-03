import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrikler
const errorRate = new Rate('errors');
const loginDuration = new Trend('login_duration');

export const options = {
    scenarios: {
        // Senaryo 1: Normal yük
        normal_load: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '1m', target: 20 },
                { duration: '2m', target: 20 },
                { duration: '1m', target: 0 },
            ],
            gracefulRampDown: '30s',
        },
        // Senaryo 2: Stres testi
        stress_test: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '1m', target: 50 },
                { duration: '2m', target: 100 },
                { duration: '1m', target: 0 },
            ],
            startTime: '5m',
        },
        // Senaryo 3: Spike testi
        spike_test: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '10s', target: 100 },
                { duration: '1m', target: 100 },
                { duration: '10s', target: 0 },
            ],
            startTime: '10m',
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<500', 'p(99)<1000'],
        http_req_failed: ['rate<0.05'],
        errors: ['rate<0.1'],
    },
};

const BASE_URL = 'http://localhost:8080';

// Test kullanıcıları
const users = [
    { email: 'test1@test.com', password: 'password123' },
    { email: 'test2@test.com', password: 'password123' },
    { email: 'test3@test.com', password: 'password123' },
];

export default function () {
    const user = users[Math.floor(Math.random() * users.length)];

    group('Health Check', function () {
        const res = http.get(`${BASE_URL}/actuator/health`);
        check(res, {
            'health status 200': (r) => r.status === 200,
            'health response time < 200ms': (r) => r.timings.duration < 200,
        });
        errorRate.add(res.status !== 200);
    });

    group('Authentication', function () {
        const loginStart = Date.now();
        const loginRes = http.post(
            `${BASE_URL}/api/auth/login`,
            JSON.stringify({
                email: user.email,
                password: user.password,
            }),
            { headers: { 'Content-Type': 'application/json' } }
        );
        loginDuration.add(Date.now() - loginStart);

        check(loginRes, {
            'login successful or unauthorized': (r) => r.status === 200 || r.status === 401,
            'login response time < 500ms': (r) => r.timings.duration < 500,
        });
        errorRate.add(loginRes.status >= 500);

        if (loginRes.status === 200) {
            const token = JSON.parse(loginRes.body).token;
            const authHeaders = {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
            };

            group('Workspaces', function () {
                const wsRes = http.get(`${BASE_URL}/api/workspaces`, authHeaders);
                check(wsRes, {
                    'workspaces status 200': (r) => r.status === 200,
                    'workspaces response time < 300ms': (r) => r.timings.duration < 300,
                });
                errorRate.add(wsRes.status !== 200);
            });
        }
    });

    sleep(Math.random() * 3 + 1);
}

export function handleSummary(data) {
    return {
        'performance-summary.json': JSON.stringify(data, null, 2),
    };
}
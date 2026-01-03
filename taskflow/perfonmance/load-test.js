import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 10 },
        { duration: '1m', target: 10 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'],
        http_req_failed: ['rate<0.1'],
    },
};

const BASE_URL = 'http://localhost:8080';

export default function () {
    let healthRes = http.get(`${BASE_URL}/actuator/health`);
    check(healthRes, {
        'health status 200': (r) => r.status === 200,
    });
    sleep(1);
}
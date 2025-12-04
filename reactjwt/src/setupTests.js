import '@testing-library/jest-dom/vitest';

if (typeof global.DOMMatrix === 'undefined') {
    class DOMMatrixMock {
        constructor() {
            this.a = this.d = 1;
            this.b = this.c = this.e = this.f = 0;
            this.is2D = true;
        }

        multiplySelf() {
            return this;
        }

        invertSelf() {
            return this;
        }

        translate() {
            return this;
        }

        scaleSelf() {
            return this;
        }
    }

    global.DOMMatrix = DOMMatrixMock;
}


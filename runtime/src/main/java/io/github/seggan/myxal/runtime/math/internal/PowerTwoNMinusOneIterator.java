package io.github.seggan.myxal.runtime.math.internal;

/*
MIT License

Copyright (c) 2017 Eric Obermühlner

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * {@link PowerIterator} to calculate x<sup>2*n-1</sup>.
 */
public class PowerTwoNMinusOneIterator implements io.github.seggan.myxal.runtime.math.internal.PowerIterator {

    private final MathContext mathContext;

    private final BigDecimal xPowerTwo;

    private BigDecimal powerOfX;

    public PowerTwoNMinusOneIterator(BigDecimal x, MathContext mathContext) {
        this.mathContext = mathContext;

        xPowerTwo = x.multiply(x, mathContext);
        powerOfX = io.github.seggan.myxal.runtime.math.internal.BigDecimalMath.reciprocal(x, mathContext);
    }

    @Override
    public BigDecimal getCurrentPower() {
        return powerOfX;
    }

    @Override
    public void calculateNextPower() {
        powerOfX = powerOfX.multiply(xPowerTwo, mathContext);
    }
}
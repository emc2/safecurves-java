/* Copyright (c) 2018, Eric McCorkle.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.metricspace.crypto.math.ec.hash;

import net.metricspace.crypto.math.ec.curve.MontgomeryCurve;
import net.metricspace.crypto.math.ec.ladder.MontgomeryLadder;
import net.metricspace.crypto.math.ec.point.ECPoint;
import net.metricspace.crypto.math.ec.point.MontgomeryPoint;
import net.metricspace.crypto.math.field.PrimeField;

/**
 * Interface for points supporting the Elligator2 hash.  Elligator2
 * was introduced by Bernstein, Hamburg, Krasnova, and Lange in their
 * paper <a
 * href="https://elligator.cr.yp.to/elligator-20130828.pdf">"Elligator:
 * Elliptic-Curve Points Indistinguishable from Uniform Random
 * Strings"</a>.  It provides the ability to hash any scalar value to
 * a point on an elliptic curve.
 * <p>
 * This is <i>not</i> a cryptogrophic hash function.  In fact,
 * Elligator provides a preimage function which produces scalar values
 * from elliptic curve points with a uniform distribution.
 * <p>
 * Elligator-2 functions on Montgomery curves of the form {@code y^2 =
 * x^3 + A * x^2 + Bx}, where {@code A * B * (A^2 - 4 * B) != 0}.
 *
 * @param <S> Scalar type.
 * @param <P> Point type.
 */
public interface Elligator2<S extends PrimeField<S>,
                            P extends Elligator2<S, P, T>,
                            T extends MontgomeryLadder.Scratchpad<S>>
    extends Elligator<S, P, T>,
            MontgomeryPoint<S, P, T>,
            MontgomeryCurve<S>  {
    /**
     * {@inheritDoc}
     */
    @Override
    public default void decodeHash(final S r,
                                   final T scratch) {
        /* Formula from https://eprint.iacr.org/2013/325.pdf
         *
         * v = -A / (1 + u * r^2)
         * e = (v^3 + A * v^2 + B * v).legendre
         * x = e * v - (1 - e) * A / 2
         * y = -e * (x^3 + A * x^2 + B * x).sqrt
         *
         * All our Montgomery curves are over 5 mod 8 primes, so u =
         * 2, and they have B = 1.  This is applied, and the formula
         * is rewritten as:
         *
         * v = -A / (1 + 2 * r^2)
         * e = ((v^2 + A * v + 1) * v).legendre
         * x = e * v + (e - 1) * A / 2
         * y = -e * ((x^2 + A * x + 1) * x).sqrt
         *
         * Manual common subexpression elimination produces the following:
         *
         * A = montgomeryA
         * C = 1 + 2 * r^2
         * V = -A / C
         * D = A * V
         * F = (V^2 + D + 1) * V
         * l1 = F.legendre
         * G = l1 * V
         * X = G + (l1 - 1) * A / 2
         * H = A * X
         * I = X^2 + H + 1
         * Y = -l1 * (I * X).sqrt
         *
         * Manual register allocation produces the following assignments:
         *
         * r0 = A
         * r1 = C
         * r2 = V
         * r1.1 = D
         * r3 = F
         * r2.1 = G
         * r3.1 = x
         * r2.2 = H
         * r1.1 = I
         * r1.2 = y
         *
         * Final formula:
         *
         * r0 = montgomeryA
         * r1 = 1 + 2 * r^2
         * r2 = -r0 / r1
         * r1.1 = r0 * r2
         * r3 = (r2^2 + r1.1 + 1) * r2
         * l1 = r3.legendre
         * r2.1 = l1 * r2
         * r3.1 = r2.1 + (l1 - 1) * r0 / 2
         * r2.2 = r0 * r3.1
         * r1.1 = r3.1^2 + r2.2 + 1
         * r1.2 = -l1 * (r1.1 * r3.1).sqrt
         * x = r3.1
         * y = r1.2
         */

        final S r0 = scratch.r0;
        final S r1 = scratch.r1;
        final S r2 = scratch.r2;
        final S r3 = scratch.r3;

        /* r0 = montgomeryA */
        r0.set(montgomeryA());

        /* r1 = 1 + 2 * r^2 */
        r1.set(r);
        r1.square();
        r1.mul(2);
        r1.add(1);

        /* r2 = -r0 / r1 */
        r2.set(r0);
        r2.neg();
        r2.div(r1, scratch);

        /* r1.1 = r0 * r2 */
        r1.set(r0);
        r1.mul(r2);

        /* r3 = (r2^2 + r1.1 + 1) * r2 */
        r3.set(r2);
        r3.square();
        r3.add(r1);
        r3.add(1);
        r3.mul(r2);

        /* l1 = r3.legendre */
        final int l1 = r3.legendre(scratch);

        /* r2.1 = l1 * r2 */
        r2.mul(l1);

        /* r3.1 = r2.1 + (l1 - 1) * r0 / 2 */
        r3.set(r0);
        r3.mul(l1 - 1);
        r3.div(2, scratch);
        r3.add(r2);

        /* r2.2 = r0 * r3.1 */
        r2.set(r0);
        r2.mul(r3);

        /* r1.1 = r3.1^2 + r2.2 + 1 */
        r1.set(r3);
        r1.square();
        r1.add(r2);
        r1.add(1);

        /* r1.2 = -l1 * (r1.1 * r3.1).sqrt */
        r1.mul(r3);
        r1.sqrt(scratch);
        r1.mul(-l1);

        /* x = r3.1 */
        /* y = r1.2 */
        setMontgomery(r3, r1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public default S encodeHash(final T scratch) {
        /* Formula from https://eprint.iacr.org/2013/325.pdf
         *
         * r = (-x / ((x + A) * u)).sqrt
         * q = (-(x + A) / (u * x)).sqrt
         * r if y is a square, q otherwise
         *
         * All our Montgomery curves are over 5 mod 8 primes, so u =
         * 2, and they have B = 1.  This is applied, and the formula
         * is rewritten as:
         *
         * r = (-x / ((x + A) * 2)).sqrt
         * q = (-(x + A) / (2 * x)).sqrt
         * r if y is a square, q otherwise
         *
         * Manual common subexpression elimination produces the following:
         *
         * C = -(x + A)
         * D = C * 2
         * E = 2 * x
         * Q = (C / E).sqrt
         * R = (x / D).sqrt
         * l1 = (y.legendre + 1) / 2
         * R if l1 = 1, Q if l1 = 0
         *
         * Manual register allocation produces the following assignments:
         *
         * r0 = C
         * r1 = D
         * r2 = E
         * r0.1 = Q
         * r2.1 = R
         *
         * Final formula:
         *
         * r0 = -(x + A)
         * r1 = r0 * 2
         * r2 = 2 * x
         * r0.1 = (r0 / r2).sqrt
         * r2.1 = (x / r1).sqrt
         * l1 = (y.legendre + 1) / 2
         * r0.2 = r2.1 if l1 = 1, r0.1 if l1 = 0
         */
        scale();

        final S r0 = scratch.r0;
        final S r1 = scratch.r1;
        final S r2 = scratch.r2;
        final S x = scratch.r3;
        final S y = scratch.r4;

        x.set(montgomeryXScaledRef(scratch));
        y.set(montgomeryYScaledRef(scratch));

        /* r0 = -(x + A) */
        r0.set(x);
        r0.add(montgomeryA());
        r0.neg();

        /* r1 = r0 * 2 */
        r1.set(r0);
        r1.mul(2);

        /* r2 = 2 * x */
        r2.set(x);
        r2.mul(2);

        /* r0.1 = (r0 / r2).sqrt
         */
        r0.div(r2, scratch);
        r0.sqrt(scratch);

        /* r2.1 = (x / r1).sqrt */
        r2.set(x);
        r2.div(r1, scratch);
        r2.sqrt(scratch);

        /* l1 = y.legendre + 1 / 2 */
        final int l1 = (y.legendre(scratch) + 1) / 2;

        /* r0.2 = r2.1 if l1 = 1, r0.1 if l1 = 0 */
        r2.mask(l1);
        r0.mask(l1 ^ 0x1);
        r0.or(r2);

        return r0.clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public default boolean canEncode(final T scratch) {
        /**
         * Formula from https://eprint.iacr.org/2013/325.pdf
         *
         * x != -a
         * y != 0 || x == 0
         * (-2 * x * (x + A)).legendre == 1
         * y == y.legendre * (x^3 + (a * x^2) + x).sqrt
         *
         * Manual common subexpression elimination produces the
         * following:
         *
         * X = montgomeryX
         * Y = montgomeryY
         * A = montgomeryA
         * MA = -A
         * b0 = X.equals(MA)
         * b1 = Y.isZero() != 1 || X.isZero == 0
         * l0 = y.legendre
         * C = X + A
         * D = X * C
         * E = (-2 * D)
         * l1 = E.legendre
         * F = ((D + 1) * X).sqrt * l0
         * b2 = Y.equals(F)
         *
         * then, !b0 && b1 && l0 == 1 && b2
         *
         * Manual register allocation produces the following assignments:
         *
         * r3 = X
         * r4 = Y
         * r0 = A
         * r1 = MA
         * r0.1 = C
         * r0.2 = D
         * r1.1 = E
         * r0.3 = F
         *
         * Final formula:
         *
         * r3 = montgomeryX
         * r4 = montgomeryY
         * r0 = montgomeryA
         * r1 = -r0
         * b0 = r3.equals(r1)
         * b1 = r4.isZero() != 1 || r3.isZero == 1
         * l0 = r4.legendre
         * r0.1 = r3 + r0
         * r0.2 = r3 * r0.1
         * r1.1 = (-2 * r0.2)
         * l1 = r1.1.legendre
         * r0.3 = ((r0.2 + 1) * r3).sqrt * l0
         * b2 = r4.equals(r0.3)
         * !b0 && b1 && l1 == 1 && b2
         */

        final S r0 = scratch.r0;
        final S r1 = scratch.r1;
        final S r3 = scratch.r3;
        final S r4 = scratch.r4;

        /* r3 = montgomeryX */
        r3.set(montgomeryXScaledRef());

        /* r4 = montgomeryY */
        r4.set(montgomeryYScaledRef());

        /* r0 = montgomeryA */
        r0.set(montgomeryA());

        /* r1 = -r0 */
        r1.set(r0);
        r1.neg();

        /* b0 = r3.equals(r1) */
        final boolean b0 = r3.equals(r1);

        /* b1 = r4.isZero() != 1 || r3.isZero == 1 */
        final boolean b1 = r4.isZero(scratch) != 1 || r3.isZero(scratch) == 1;

        /* l0 = y.legendre */
        final int l0 = r4.legendre(scratch);

        /* r0.1 = r3 + r0 */
        r0.add(r3);

        /* r0.2 = r3 * r0.1 */
        r0.mul(r3);

        /* r1.1 = (-2 * r0.2) */
        r1.set(r0);
        r1.mul(-2);

        /* l1 = r1.1.legendre */
        final int l1 = r1.legendre(scratch);

        /* r0.3 = ((r0.2 + 1) * r3).sqrt * l0 */
        r0.add(1);
        r0.mul(r3);
        r0.sqrt(scratch);
        r0.mul(l0);

        /* b2 = r4.equals(r0.3) */
        final boolean b2 = r4.equals(r0);

        /* !b0 && b1 && l1 == 1 && b2 */
        return !b0 && b1 && l1 == 1 && b2;
    }
}

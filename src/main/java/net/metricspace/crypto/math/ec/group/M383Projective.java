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
package net.metricspace.crypto.math.ec.group;

import net.metricspace.crypto.math.ec.curve.M383Curve;
import net.metricspace.crypto.math.ec.point.M383ProjectivePoint;
import net.metricspace.crypto.math.field.ModE383M187;

/**
 * The M-383 elliptic curve.  This curve was introduced by Aranha,
 * Barreto, Periera, and Ricardini in their paper <a
 * href="https://eprint.iacr.org/2013/647.pdf">"A Note on
 * High-Security General-Purpose Elliptic Curves"</a>.  It is defined
 * by the Montgomery-form equation {@code y^2 = x^3 + 2065150 * x^2 *
 * x} over the prime field {@code mod 2^383 - 187}, and provides
 * roughly {@code 189.8} bits of security against the Pollard-Rho
 * attack.
 * <p>
 * This curve is also birationally equivalent to the twisted Edwards
 * curve {@code 2065152 * x^2 + y^2 = 1 + 2065148 * x^2 * y^2}.
 * <p>
 * This group uses the projective point representation.
 *
 * @see ModE383M187
 * @see net.metricspace.crypto.math.ec.curve.M383Curve
 */
public class M383Projective
    extends M383<M383ProjectivePoint, M383ProjectivePoint.Scratchpad>
    implements M383Curve,
               ElligatorGroup<ModE383M187, M383ProjectivePoint,
                              M383ProjectivePoint.Scratchpad> {
    /**
     * The base point of the M-383 group.
     */
    private static M383ProjectivePoint BASE_POINT =
        M383ProjectivePoint.fromMontgomery(baseX(), baseY());

    /**
     * The zero point of the M-383 group.
     */
    private static M383ProjectivePoint ZERO_POINT =
        M383ProjectivePoint.zero();

    /**
     * {@inheritDoc}
     */
    @Override
    public M383ProjectivePoint.Scratchpad scratchpad() {
        return M383ProjectivePoint.Scratchpad.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public M383ProjectivePoint
        fromTwistedEdwards(final ModE383M187 x,
                           final ModE383M187 y) {
        return M383ProjectivePoint.fromEdwards(x, y);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public M383ProjectivePoint
        fromMontgomery(final ModE383M187 x,
                       final ModE383M187 y,
                       final M383ProjectivePoint.Scratchpad scratch) {
        return M383ProjectivePoint.fromMontgomery(x, y, scratch);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public M383ProjectivePoint
        fromHash(final ModE383M187 r,
                 final M383ProjectivePoint.Scratchpad scratch) {
        return M383ProjectivePoint.fromHash(r, scratch);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public M383ProjectivePoint basePoint() {
        return BASE_POINT.clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public M383ProjectivePoint zeroPoint() {
        return ZERO_POINT.clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int cofactor() {
        return 8;
    }
}

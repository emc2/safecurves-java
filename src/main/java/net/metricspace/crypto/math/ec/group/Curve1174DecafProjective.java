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

import net.metricspace.crypto.math.ec.curve.Curve1174Curve;
import net.metricspace.crypto.math.ec.point.Curve1174DecafProjectivePoint;
import net.metricspace.crypto.math.field.ModE251M9;

/**
 * The Curve1174 elliptic curve group with Decaf point compression.
 * This curve was introduced by Bernstein, Hamburg, Krasnova, and
 * Lange in their paper <a
 * href="https://eprint.iacr.org/2013/325.pdf">"Elligator:
 * Elliptic-Curve Points Indistinguishable from Uniform Random
 * Strings"</a>.  It is defined by the equation {@code x^2 + y^2 = 1 -
 * 1174 * x^2 * y^2} over the prime field {@code mod 2^251 - 9}, and
 * provides roughly {@code 124.3} bits of security against the
 * Pollard-Rho attack.
 * <p>
 * Decaf point compression was described by Hamburg in his paper <a
 * href="https://eprint.iacr.org/2015/673.pdf">"Decaf: Eliminating
 * Cofactors through Point Compression"</a>.  It reduces the cofactor
 * by a factor of {@code 4}
 * <p>
 * This group uses the projective point representation.
 *
 * @see ModE251M9
 * @see net.metricspace.crypto.math.ec.curve.Curve1174Curve
 */
public class Curve1174DecafProjective
    extends Curve1174Decaf<Curve1174DecafProjectivePoint,
                           Curve1174DecafProjectivePoint.Scratchpad>
    implements Curve1174Curve,
               ElligatorGroup<ModE251M9, Curve1174DecafProjectivePoint,
                              Curve1174DecafProjectivePoint.Scratchpad> {
    /**
     * The base point of the Curve1174 group.
     */
    private static Curve1174DecafProjectivePoint BASE_POINT =
        Curve1174DecafProjectivePoint.fromEdwards(baseX(), baseY());

    /**
     * The zero point of the Curve1174 group.
     */
    private static Curve1174DecafProjectivePoint ZERO_POINT =
        Curve1174DecafProjectivePoint.fromEdwards(ModE251M9.zero(),
                                                  ModE251M9.one());

    /**
     * {@inheritDoc}
     */
    @Override
    public Curve1174DecafProjectivePoint.Scratchpad scratchpad() {
        return Curve1174DecafProjectivePoint.Scratchpad.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Curve1174DecafProjectivePoint
        fromEdwards(final ModE251M9 x,
                    final ModE251M9 y) {
        return Curve1174DecafProjectivePoint.fromEdwards(x, y);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Curve1174DecafProjectivePoint
        fromCompressed(final ModE251M9 s,
                       final Curve1174DecafProjectivePoint.Scratchpad scratch)
        throws IllegalArgumentException {
        return Curve1174DecafProjectivePoint.fromCompressed(s, scratch);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Curve1174DecafProjectivePoint
        fromHash(final ModE251M9 r,
                 final Curve1174DecafProjectivePoint.Scratchpad scratch) {
        return Curve1174DecafProjectivePoint.fromHash(r, scratch);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Curve1174DecafProjectivePoint basePoint() {
        return BASE_POINT.clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Curve1174DecafProjectivePoint zeroPoint() {
        return ZERO_POINT.clone();
    }
}

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
package net.metricspace.crypto.math.ec.point;

import java.lang.ThreadLocal;

import net.metricspace.crypto.math.ec.curve.E222Curve;
import net.metricspace.crypto.math.ec.hash.Elligator1;
import net.metricspace.crypto.math.field.ModE222M117;

/**
 * Extended coordinates on the Edwards curve E-222.
 */
public class E222ExtendedPoint
    extends ExtendedEdwardsPoint<ModE222M117, E222ExtendedPoint,
                                 E222ExtendedPoint.Scratchpad>
    implements E222Curve,
               Elligator1<ModE222M117, E222ExtendedPoint,
                          E222ExtendedPoint.Scratchpad> {
    /**
     * Scratchpads for projective E-222 points.
     */
    public static final class Scratchpad
        extends ExtendedEdwardsPoint.Scratchpad<ModE222M117> {

        private static final ThreadLocal<Scratchpad> scratchpads =
            new ThreadLocal<Scratchpad>() {
                @Override
                public Scratchpad initialValue() {
                    return new Scratchpad();
                }
            };

        /**
         * Initialize an empty {@code Scratchpad}.
         */
        private Scratchpad() {
            super(new ModE222M117(0), new ModE222M117(0), new ModE222M117(0),
                  new ModE222M117(0), new ModE222M117(0), new ModE222M117(0),
                  ModE222M117.NUM_DIGITS);
        }

        /**
         * Get an instance of this {@code Scratchpad}.
         *
         * @return An instance of this {@code Scratchpad}.
         */
        public static Scratchpad get() {
            return scratchpads.get();
        }
    }

    /**
     * Initialize an {@code E222ExtendedPoint} with zero coordinates.
     */
    private E222ExtendedPoint() {
        this(new ModE222M117(0), new ModE222M117(1),
             new ModE222M117(1), new ModE222M117(0));
    }

    /**
     * Initialize an {@code E222ExtendedPoint} with raw Edwards X and
     * Y coordinates.  This constructor takes possession of the
     * parameters.
     *
     * @param x The X coordinate value.
     * @param y The Y coordinate value.
     */
    protected E222ExtendedPoint(final ModE222M117 x,
                                final ModE222M117 y) {
        super(x, y);
    }

    /**
     * Initialize an {@code E222ExtendedPoint} with four scalar
     * objects.  This constructor takes possession of the parameters,
     * which are used as the coordinate objects.
     *
     * @param x The scalar object for x.
     * @param y The scalar object for y.
     * @param z The scalar object for z.
     * @param t The scalar object for t.
     */
    protected E222ExtendedPoint(final ModE222M117 x,
                                final ModE222M117 y,
                                final ModE222M117 z,
                                final ModE222M117 t) {
        super(x, y, z, t);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModE222M117 elligatorS() {
        return E222Curve.ELLIGATOR_S.clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModE222M117 elligatorR() {
        return E222Curve.ELLIGATOR_R.clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModE222M117 elligatorC() {
        return E222Curve.ELLIGATOR_C.clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Scratchpad scratchpad() {
        return Scratchpad.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E222ExtendedPoint clone() {
        return new E222ExtendedPoint(x.clone(), y.clone(),
                                     z.clone(), t.clone());
    }

    /**
     * Create a {@code E222ExtendedPoint} initialized as the
     * zero-point on the curve E-222 in extended coordinates.
     *
     * @return A zero point on the curve E-222 in extended
     *         coordinates.
     */
    public static E222ExtendedPoint zero() {
        return new E222ExtendedPoint();
    }

    /**
     * Create a {@code E222ExtendedPoint} initialized from Edwards
     * {@code x} and {@code y} points.
     *
     * @param x The Edwards {@code x} coordinate.
     * @param y The Edwards {@code y} coordinate.
     * @return A point initialized to the given Edwards {@code x} and
     *         {@code y} coordinates.
     */
    public static E222ExtendedPoint fromEdwards(final ModE222M117 x,
                                                final ModE222M117 y) {
        return new E222ExtendedPoint(x.clone(), y.clone());
    }

    /**
     * Create a {@code E222ExtendedPoint} from a hash.
     *
     * @param s The hash input.
     * @param scratch The scratchpad to use.
     * @return A point initialized by hashing {@code s} to a point.
     * @throws IllegalArgumentException If the hash input is invalid.
     */
    public static E222ExtendedPoint fromHash(final ModE222M117 s,
                                             final Scratchpad scratch)
        throws IllegalArgumentException {
        final E222ExtendedPoint p = zero();

        p.decodeHash(s, scratch);

        return p;
    }
}

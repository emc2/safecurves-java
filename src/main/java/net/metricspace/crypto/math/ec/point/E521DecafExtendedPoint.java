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

import net.metricspace.crypto.math.ec.curve.E521Curve;
import net.metricspace.crypto.math.ec.hash.ElligatorDecaf;
import net.metricspace.crypto.math.field.ModE521M1;

/**
 * Extended coordinates on the Edwards curve E-521 with Decaf point
 * compression.
 */
public class E521DecafExtendedPoint
    extends ExtendedEdwardsDecafPoint<ModE521M1, E521DecafExtendedPoint,
                                      E521DecafExtendedPoint.Scratchpad>
    implements E521Curve,
               ElligatorDecaf<ModE521M1, E521DecafExtendedPoint,
                              E521DecafExtendedPoint.Scratchpad> {
    /**
     * Scratchpads for extended E-521 points.
     */
    public static final class Scratchpad
        extends ExtendedEdwardsPoint.Scratchpad<ModE521M1> {

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
            super(new ModE521M1(0), new ModE521M1(0), new ModE521M1(0),
                  new ModE521M1(0), new ModE521M1(0), new ModE521M1(0),
                  ModE521M1.NUM_DIGITS);
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
     * Initialize a {@code E521DecafExtendedPoint} with zero coordinates.
     */
    private E521DecafExtendedPoint() {
        this(new ModE521M1(0), new ModE521M1(1),
             new ModE521M1(1), new ModE521M1(0));
    }

    /**
     * Initialize an {@code E521DecafExtendedPoint} with raw Edwards X
     * and Y coordinates.  This constructor takes possession of the
     * parameters.
     *
     * @param x The X coordinate value.
     * @param y The Y coordinate value.
     */
    protected E521DecafExtendedPoint(final ModE521M1 x,
                                     final ModE521M1 y) {
        super(x, y);
    }

    /**
     * Initialize an {@code E521DecafExtendedPoint} with four scalar
     * objects.  This constructor takes possession of the parameters,
     * which are used as the coordinate objects.
     *
     * @param x The scalar object for x.
     * @param y The scalar object for y.
     * @param z The scalar object for z.
     * @param t The scalar object for t.
     */
    protected E521DecafExtendedPoint(final ModE521M1 x,
                                     final ModE521M1 y,
                                     final ModE521M1 z,
                                     final ModE521M1 t) {
        super(x, y, z, t);
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
    public E521DecafExtendedPoint clone() {
        return new E521DecafExtendedPoint(x.clone(), y.clone(),
                                          z.clone(), t.clone());
    }

    /**
     * Create a {@code E521DecafExtendedPoint} initialized as the
     * zero-point on the curve E-521 in extended coordinates.
     *
     * @return A zero point on the curve E-521 in extended
     *         coordinates.
     */
    public static E521DecafExtendedPoint zero() {
        return new E521DecafExtendedPoint();
    }

    /**
     * Create a {@code E521DecafExtendedPoint} initialized from Edwards
     * {@code x} and {@code y} points.
     *
     * @param x The Edwards {@code x} coordinate.
     * @param y The Edwards {@code y} coordinate.
     * @return A point initialized to the given Edwards {@code x} and
     *         {@code y} coordinates.
     */
    public static E521DecafExtendedPoint fromEdwards(final ModE521M1 x,
                                                     final ModE521M1 y) {
        return new E521DecafExtendedPoint(x.clone(), y.clone());
    }

    /**
     * Create a {@code E521DecafExtendedPoint} by decompressing a
     * compressed point.
     *
     * @param s The compressed point.
     * @return A point initialized by decompressing {@code s}
     * @throws IllegalArgumentException If the compressed point is invalid.
     */
    public static E521DecafExtendedPoint fromCompressed(final ModE521M1 s)
        throws IllegalArgumentException {
        try(final Scratchpad scratch = Scratchpad.get()) {
            return fromCompressed(s, scratch);
        }
    }

    /**
     * Create a {@code E521DecafExtendedPoint} by decompressing a
     * compressed point.
     *
     * @param s The compressed point.
     * @param scratch The scratchpad to use.
     * @return A point initialized by decompressing {@code s}
     * @throws IllegalArgumentException If the compressed point is invalid.
     */
    public static E521DecafExtendedPoint
        fromCompressed(final ModE521M1 s,
                       final Scratchpad scratch)
        throws IllegalArgumentException {
        final E521DecafExtendedPoint p = zero();

        p.decompress(s, scratch);

        return p;
    }

    /**
     * Create a {@code E521DecafExtendedPoint} from a hash.
     *
     * @param r The hash input.
     * @return A point initialized by hashing {@code r} to a point.
     * @throws IllegalArgumentException If the hash input is invalid.
     */
    public static E521DecafExtendedPoint fromHash(final ModE521M1 r)
        throws IllegalArgumentException {
        try(final Scratchpad scratch = Scratchpad.get()) {
            return fromHash(r, scratch);
        }
    }

    /**
     * Create a {@code E521DecafExtendedPoint} from a hash.
     *
     * @param r The hash input.
     * @param scratch The scratchpad to use.
     * @return A point initialized by hashing {@code r} to a point.
     * @throws IllegalArgumentException If the hash input is invalid.
     */
    public static E521DecafExtendedPoint fromHash(final ModE521M1 r,
                                                  final Scratchpad scratch)
        throws IllegalArgumentException {
        final E521DecafExtendedPoint p = zero();

        p.decodeHash(r, scratch);

        return p;
    }
}

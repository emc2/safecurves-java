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

import net.metricspace.crypto.math.ec.curve.MontgomeryBirationalEquivalence;
import net.metricspace.crypto.math.ec.curve.TwistedEdwardsCurve;
import net.metricspace.crypto.math.field.PrimeField;

/**
 * Points on a twisted Edwards curve, which has the form {@code a *
 * x^2 + y^2 = 1 + d * x^2 * y^2 }.  All twisted Edwards curves are
 * birationally equivalent to a Montgomery curve.
 *
 * @param <S> The scalar field type.
 * @param <P> Point type.
 */
public interface TwistedEdwardsPoint<S extends PrimeField<S>,
                                     P extends TwistedEdwardsPoint<S, P, T>,
                                     T extends ECPoint.Scratchpad<S>>
    extends MontgomeryPoint<S, P, T>,
            MontgomeryBirationalEquivalence<S> {
    /**
     * Get the value of the X coordinate in the Edwards
     * representation.
     *
     * @return The value of the X coordinate in the Edwards
     * representation.
     */
    public default S edwardsX() {
        scale();

        return edwardsXScaled();
    }

    /**
     * Get the value of the Y coordinate in the Edwards
     * representation.
     *
     * @return The value of the Y coordinate in the Edwards
     * representation.
     */
    public default S edwardsY() {
        scale();

        return edwardsYScaled();
    }

    /**
     * Get the value of the X coordinate in the Edwards
     * representation.  This assumes the point has already been
     * scaled.
     *
     * @return The value of the X coordinate in the Edwards
     * representation.
     * @see #scale()
     */
    public default S edwardsXScaled() {
        return edwardsXScaledRef().clone();
    }

    /**
     * Get the value of the Y coordinate in the Edwards
     * representation.  This assumes the point has already been
     * scaled.
     *
     * @return The value of the Y coordinate in the Edwards
     * representation.
     * @see #scale()
     */
    public default S edwardsYScaled() {
        return edwardsYScaledRef().clone();
    }

    /**
     * Get a direct reference to the value of the X coordinate in the
     * Edwards representation.  This assumes the point has already
     * been scaled.
     *
     * @return The value of the X coordinate in the Edwards
     * representation.
     * @see #scale()
     */
    public S edwardsXScaledRef();

    /**
     * Get a direct reference to the value of the Y coordinate in the
     * Edwards representation.  This assumes the point has already
     * been scaled.
     *
     * @return The value of the Y coordinate in the Edwards
     * representation.
     * @see #scale()
     */
    public S edwardsYScaledRef();

    /**
     * Set the point from its Edwards coordinates.
     *
     * @param x The Edwards X coordinate.
     * @param y The Edwards Y coordinate.
     */
    public void setEdwards(final S x,
                           final S y);

    /**
     * Set Edwards X and Y scalar coordinate object from Montgomery
     * coordinates.
     *
     * @param <S> The scalar field type.
     * @param u The Montgomery {@code u} (or {@code x})
     * @param v The Montgomery {@code v} (or {@code y})
     * @param x The Edwards {@code x} coordinate to set.
     * @param y The Edwards {@code y} coordinate to set.
     * @param scratch The scratchpad to use.
     */
    public static <S extends PrimeField<S>,
                   T extends ECPoint.Scratchpad<S>>
        void montgomeryToEdwards(final S u,
                                 final S v,
                                 final S x,
                                 final S y,
                                 final T scratch) {
        final S ydenom = scratch.r2;

        ydenom.set(u);
        x.set(v);
        x.inv();
        x.mul(u);
        ydenom.add(1);
        ydenom.inv();
        y.set(u);
        y.sub(1);
        y.mul(ydenom);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public default S montgomeryXScaledRef(final T scratch) {
        final S y = scratch.r0;
        final S denom = scratch.r1;

        y.set(edwardsYScaledRef());
        denom.set(y);
        denom.sub(1);
        denom.neg();
        denom.inv();
        y.add(1);
        y.mul(denom);

        return y;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public default S montgomeryYScaledRef(final T scratch) {
        final S y = scratch.r0;
        final S denom = scratch.r1;

        y.set(edwardsYScaledRef());
        denom.set(y);
        denom.sub(1);
        denom.neg();
        denom.mul(edwardsXScaledRef());
        denom.inv();
        y.add(1);
        y.mul(denom);

        return y;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public default void setMontgomery(final S u,
                                      final S v,
                                      final T scratch) {
        final S x = scratch.r0;
        final S y = scratch.r1;

        montgomeryToEdwards(u, v, x, y, scratch);
        setEdwards(x, y);
    }

    /**
     * Get the value of the X coordinate in the Montgomery
     * representation.
     *
     * @param scratch The scratchpad to use.
     * @return The value of the X coordinate in the Montgomery
     * representation.
     */
    @Override
    public default S getXScaledRef(final T scratch) {
        return montgomeryXScaledRef(scratch);
    }

    /**
     * Get the value of the Y coordinate in the Montgomery
     * representation.
     *
     * @param scratch The scratchpad to use.
     * @return The value of the Y coordinate in the Montgomery
     * representation.
     */
    @Override
    public default S getYScaledRef(final T scratch) {
        return montgomeryYScaledRef(scratch);
    }

    /**
     * Set the point from its Montgomery coordinates.
     *
     * @param x The Montgomery X coordinate.
     * @param y The Montgomery Y coordinate.
     */
    @Override
    public default void set(final S x,
                            final S y) {
        setMontgomery(x, y);
    }
}

/*
 * Copyright 1999-2101 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.simpleimage.codec.convertor;

/**
 * TODO Comment of FastInverseDCTCalculator
 * 
 * @author wendell
 */
public class FastInverseDCTCalculator extends InverseDCTCalculator {

    public static final int FIX_1_414213562 = 362;
    public static final int FIX_1_847759065 = 473;
    public static final int FIX_1_082392200 = 277;
    public static final int FIX_2_613125930 = 669;

    public static final int PASS1_BITS      = 2;
    public static final int CONST_BITS      = 8;

    /**
     * AA&N DCT algorithm implemention coeff # in, dct coefficients, length = 64 data # out, 64 bytes
     */
    @Override
    public Object calculate(int[] coeff, int inPos, int[] quant, int[] out, int outOffset, int width, int height) {
        int tmp0, tmp1, tmp2, tmp3, tmp4, tmp5, tmp6, tmp7;
        int tmp10, tmp11, tmp12, tmp13;
        int z5, z10, z11, z12, z13;

        int[] workspace = new int[64];

        int inptr = 0;
        int wsptr = 0;
        int[] outbuf = (out != null ? out : new int[DCTSIZE2]);
        int outptr = 0;
        short[] rangeLimitMap = sampleRangeLimitTable;
        int rangeLimitOffset = sampleRangeLimitOffset + 128;
        int dcval = 0, DCTSIZE = 8;

        int quantptr = 0;

        for (int ctr = 8; ctr > 0; ctr--) {
            int basis = 0;
            for (int n = 1; n < 8; n++) {
                basis |= coeff[inptr + DCTSIZE * n];
            }

            if (basis == 0) {
                dcval = coeff[inptr + DCTSIZE * 0] * quant[quantptr + DCTSIZE * 0];

                workspace[wsptr + DCTSIZE * 0] = dcval;
                workspace[wsptr + DCTSIZE * 1] = dcval;
                workspace[wsptr + DCTSIZE * 2] = dcval;
                workspace[wsptr + DCTSIZE * 3] = dcval;
                workspace[wsptr + DCTSIZE * 4] = dcval;
                workspace[wsptr + DCTSIZE * 5] = dcval;
                workspace[wsptr + DCTSIZE * 6] = dcval;
                workspace[wsptr + DCTSIZE * 7] = dcval;

                inptr++;
                quantptr++;
                wsptr++;

                continue;
            }

            tmp0 = coeff[inptr + DCTSIZE * 0] * quant[quantptr + DCTSIZE * 0];
            tmp1 = coeff[inptr + DCTSIZE * 2] * quant[quantptr + DCTSIZE * 2];
            tmp2 = coeff[inptr + DCTSIZE * 4] * quant[quantptr + DCTSIZE * 4];
            tmp3 = coeff[inptr + DCTSIZE * 6] * quant[quantptr + DCTSIZE * 6];
            tmp10 = tmp0 + tmp2;
            tmp11 = tmp0 - tmp2;

            tmp13 = tmp1 + tmp3;

            tmp12 = MULTIPLY(tmp1 - tmp3, FIX_1_414213562) - tmp13;

            tmp0 = tmp10 + tmp13;
            tmp3 = tmp10 - tmp13;
            tmp1 = tmp11 + tmp12;
            tmp2 = tmp11 - tmp12;

            tmp4 = coeff[inptr + DCTSIZE * 1] * quant[quantptr + DCTSIZE * 1];
            tmp5 = coeff[inptr + DCTSIZE * 3] * quant[quantptr + DCTSIZE * 3];
            tmp6 = coeff[inptr + DCTSIZE * 5] * quant[quantptr + DCTSIZE * 5];
            tmp7 = coeff[inptr + DCTSIZE * 7] * quant[quantptr + DCTSIZE * 7];

            z13 = tmp6 + tmp5;
            z10 = tmp6 - tmp5;
            z11 = tmp4 + tmp7;
            z12 = tmp4 - tmp7;

            tmp7 = z11 + z13;
            tmp11 = MULTIPLY(z11 - z13, FIX_1_414213562);

            z5 = MULTIPLY(z10 + z12, FIX_1_847759065);
            tmp10 = MULTIPLY(z12, FIX_1_082392200) - z5;
            tmp12 = MULTIPLY(z10, -FIX_2_613125930) + z5;

            tmp6 = tmp12 - tmp7;
            tmp5 = tmp11 - tmp6;
            tmp4 = tmp10 + tmp5;

            workspace[wsptr + DCTSIZE * 0] = (tmp0 + tmp7);
            workspace[wsptr + DCTSIZE * 7] = (tmp0 - tmp7);
            workspace[wsptr + DCTSIZE * 1] = (tmp1 + tmp6);
            workspace[wsptr + DCTSIZE * 6] = (tmp1 - tmp6);
            workspace[wsptr + DCTSIZE * 2] = (tmp2 + tmp5);
            workspace[wsptr + DCTSIZE * 5] = (tmp2 - tmp5);
            workspace[wsptr + DCTSIZE * 4] = (tmp3 + tmp4);
            workspace[wsptr + DCTSIZE * 3] = (tmp3 - tmp4);

            inptr++;
            quantptr++;
            wsptr++;
        }

        wsptr = 0;
        for (int ctr = 0; ctr < DCTSIZE; ctr++) {
            outptr = ctr * 8;

            int basis = 0;
            for (int u = wsptr + 1; u < wsptr + 8; u++) {
                basis |= workspace[u];
            }
            if (basis == 0) {
                // AC terms all zero
                dcval = rangeLimitMap[rangeLimitOffset + ((workspace[wsptr] >> 5) & RANGE_MASK)];

                outbuf[outptr + 0] = dcval;
                outbuf[outptr + 1] = dcval;
                outbuf[outptr + 2] = dcval;
                outbuf[outptr + 3] = dcval;
                outbuf[outptr + 4] = dcval;
                outbuf[outptr + 5] = dcval;
                outbuf[outptr + 6] = dcval;
                outbuf[outptr + 7] = dcval;

                wsptr += DCTSIZE;
                continue;
            }

            tmp10 = (workspace[wsptr + 0] + workspace[wsptr + 4]);
            tmp11 = (workspace[wsptr + 0] - workspace[wsptr + 4]);

            tmp13 = (workspace[wsptr + 2] + workspace[wsptr + 6]);
            tmp12 = MULTIPLY(workspace[wsptr + 2] - workspace[wsptr + 6], FIX_1_414213562) - tmp13;

            tmp0 = tmp10 + tmp13;
            tmp3 = tmp10 - tmp13;
            tmp1 = tmp11 + tmp12;
            tmp2 = tmp11 - tmp12;

            z13 = workspace[wsptr + 5] + workspace[wsptr + 3];
            z10 = workspace[wsptr + 5] - workspace[wsptr + 3];
            z11 = workspace[wsptr + 1] + workspace[wsptr + 7];
            z12 = workspace[wsptr + 1] - workspace[wsptr + 7];

            tmp7 = z11 + z13;
            tmp11 = MULTIPLY(z11 - z13, FIX_1_414213562);

            z5 = MULTIPLY(z10 + z12, FIX_1_847759065);
            tmp10 = MULTIPLY(z12, FIX_1_082392200) - z5;
            tmp12 = MULTIPLY(z10, (-FIX_2_613125930)) + z5;

            tmp6 = tmp12 - tmp7;
            tmp5 = tmp11 - tmp6;
            tmp4 = tmp10 + tmp5;

            outbuf[outptr + 0] = rangeLimitMap[rangeLimitOffset + (IDESCALE(tmp0 + tmp7, PASS1_BITS + 3) & RANGE_MASK)];
            outbuf[outptr + 7] = rangeLimitMap[rangeLimitOffset + (IDESCALE(tmp0 - tmp7, PASS1_BITS + 3) & RANGE_MASK)];
            outbuf[outptr + 1] = rangeLimitMap[rangeLimitOffset + (IDESCALE(tmp1 + tmp6, PASS1_BITS + 3) & RANGE_MASK)];
            outbuf[outptr + 6] = rangeLimitMap[rangeLimitOffset + (IDESCALE(tmp1 - tmp6, PASS1_BITS + 3) & RANGE_MASK)];
            outbuf[outptr + 2] = rangeLimitMap[rangeLimitOffset + (IDESCALE(tmp2 + tmp5, PASS1_BITS + 3) & RANGE_MASK)];
            outbuf[outptr + 5] = rangeLimitMap[rangeLimitOffset + (IDESCALE(tmp2 - tmp5, PASS1_BITS + 3) & RANGE_MASK)];
            outbuf[outptr + 4] = rangeLimitMap[rangeLimitOffset + (IDESCALE(tmp3 + tmp4, PASS1_BITS + 3) & RANGE_MASK)];
            outbuf[outptr + 3] = rangeLimitMap[rangeLimitOffset + (IDESCALE(tmp3 - tmp4, PASS1_BITS + 3) & RANGE_MASK)];

            wsptr += DCTSIZE;
        }

        return outbuf;
    }

    private int MULTIPLY(int var, int cons) {
        return (var * cons) >> 8;
    }

    private int IDESCALE(int x, int n) {
        return x >> n;
    }

}

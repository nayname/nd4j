/*-
 *
 *  * Copyright 2015 Skymind,Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 *
 */

package org.nd4j.linalg.api.ops.impl.accum;

import lombok.val;
import org.nd4j.autodiff.samediff.SDVariable;
import org.nd4j.autodiff.samediff.SameDiff;
import org.nd4j.linalg.api.blas.params.MMulTranspose;
import org.nd4j.linalg.api.ops.DynamicCustomOp;
import org.nd4j.linalg.api.shape.Shape;
import org.nd4j.linalg.exception.ND4JIllegalStateException;
import org.nd4j.linalg.util.ArrayUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Matrix multiplication/dot product
 *
 * @author Adam Gibson
 */
public class Mmul extends DynamicCustomOp {

    protected MMulTranspose mMulTranspose;

    /**
     *
     * @param sameDiff
     * @param i_v1
     * @param i_v2
     * @param mMulTranspose
     */
    public Mmul(SameDiff sameDiff,
                SDVariable i_v1,
                SDVariable i_v2,
                MMulTranspose mMulTranspose) {
        super(null,sameDiff,new SDVariable[]{i_v1,i_v2});
        this.mMulTranspose = mMulTranspose;
        addIArgument(fromBoolean(mMulTranspose.isTransposeA()),fromBoolean(mMulTranspose.isTransposeB()));
    }


    /**
     *
     * @param sameDiff
     * @param i_v1
     * @param i_v2
     */
    public Mmul(SameDiff sameDiff,
                SDVariable i_v1,
                SDVariable i_v2) {
        this(sameDiff,i_v1,i_v2,MMulTranspose.allFalse());
    }



    public Mmul() {}


    @Override
    public List<int[]> calculateOutputShape() {
        if(mMulTranspose == null)
            mMulTranspose = MMulTranspose.allFalse();
        List<int[]> ret = new ArrayList<>(1);
        int[] aShape = mMulTranspose.isTransposeA() ? ArrayUtil.reverseCopy(larg().getShape()) : larg().getShape();
        int[] bShape = mMulTranspose.isTransposeB() ? ArrayUtil.reverseCopy(rarg().getShape()) : rarg().getShape();
        if(Shape.isPlaceholderShape(aShape) || Shape.isPlaceholderShape(bShape))
            return Collections.emptyList();

        if(aShape != null && bShape != null) {
            val shape =  Shape.getMatrixMultiplyShape(aShape,bShape);
            ret.add(shape);
        }
        if(!ret.isEmpty()) {
            for(int i = 0; i < ret.get(0).length; i++) {
                if(ret.get(0)[i] < 1)
                    throw new ND4JIllegalStateException("Invalid shape computed at index " +  i);
            }
        }
        return ret;
    }


    @Override
    public String onnxName() {
        return "MatMul";
    }

    @Override
    public String tensorflowName() {
        return "MatMul";
    }



    @Override
    public String opName() {
        return "mmul";
    }



    @Override
    public List<SDVariable> doDiff(List<SDVariable> i_v1) {
        List<SDVariable> ret = new ArrayList<>();
        SDVariable setup = sameDiff.setupFunction(i_v1.get(0));
        SDVariable gradWrtX = sameDiff.setupFunction(f().reshape(f().mmul(setup,rarg(),
                MMulTranspose.builder()
                        .transposeB(!mMulTranspose.isTransposeB())
                        .transposeResult(mMulTranspose.isTransposeA())
                        .build()),larg().getShape()));

        SDVariable gradWrtY = sameDiff.setupFunction(f().reshape(f().mmul(larg(),setup,
                MMulTranspose.builder()
                        .transposeA(!mMulTranspose.isTransposeA())
                        .transposeResult(mMulTranspose.isTransposeB())
                        .build()),rarg().getShape()));

        ret.add(gradWrtX);
        ret.add(gradWrtY);
        return ret;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Mmul mmul = (Mmul) o;

        return mMulTranspose != null ? mMulTranspose.equals(mmul.mMulTranspose) : mmul.mMulTranspose == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (mMulTranspose != null ? mMulTranspose.hashCode() : 0);
        return result;
    }
}


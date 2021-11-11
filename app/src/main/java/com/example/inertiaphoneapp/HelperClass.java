package com.example.inertiaphoneapp;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

public class HelperClass {


    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    public  static void Vector_Cross_Product(ArrayList<Float> out, ArrayList<Float> v1, ArrayList<Float> v2)
    {
        out.set(0, (v1.get(1) * v2.get(2)) - (v1.get(2) * v2.get(1)));
        out.set(1, (v1.get(2) * v2.get(0)) - (v1.get(0) * v2.get(2)));
        out.set(2, (v1.get(0) * v2.get(1)) - (v1.get(1) * v2.get(0)));
    }

    float Vector_Dot_Product(ArrayList<Float> v1, ArrayList<Float> v2)
    {
        float result = 0;
        for(int c = 0; c < 3; c++)
        {
            result += v1.get(c) * v2.get(c);
        }
        return result;
    }

    // Multiply the vector by a scalar
    void Vector_Scale(ArrayList<Float> out, ArrayList<Float>  v, float scale)
    {
        for(int c = 0; c < 3; c++)
        {
            out.set(c, v.get(c) * scale);
        }
    }

    // Adds two vectors
    void Vector_Add(ArrayList<Float> out, ArrayList<Float> v1, ArrayList<Float> v2)
    {
        for(int c = 0; c < 3; c++)
        {
            out.set(c, v1.get(c) + v2.get(c));
        }
    }

    // Multiply two 3x3 matrices: out = a * b
// out has to different from a and b (no in-place)!
    void Matrix_Multiply(final float a[][], final float b[][],final float out[][])
    {
        for(int x = 0; x < 3; x++)  // rows
        {
            for(int y = 0; y < 3; y++)  // columns
            {
                out[x][y] = a[x][0] * b[0][y] + a[x][1] * b[1][y] + a[x][2] * b[2][y];
            }
        }
    }

    // Multiply 3x3 matrix with vector: out = a * b
// out has to different from b (no in-place)!
    void Matrix_Vector_Multiply(final float a[][],final float b[],final float out[])
    {
        for(int x = 0; x < 3; x++)
        {
            out[x] = a[x][0] * b[0] + a[x][1] * b[1] + a[x][2] * b[2];
        }
    }

    // Init rotation matrix using euler angles
    void init_rotation_matrix(double m[][], double yaw, double pitch, double roll)
    {
        double c1 = Math.cos(roll);
        double s1 = Math.sin(roll);
        double c2 = Math.cos(pitch);
        double s2 = Math.sin(pitch);
        double c3 = Math.cos(yaw);
        double s3 = Math.sin(yaw);

        // Euler angles, right-handed, intrinsic, XYZ convention
        // (which means: rotate around body axes Z, Y', X'')
        m[0][0] = c2 * c3;
        m[0][1] = c3 * s1 * s2 - c1 * s3;
        m[0][2] = s1 * s3 + c1 * c3 * s2;

        m[1][0] = c2 * s3;
        m[1][1] = c1 * c3 + s1 * s2 * s3;
        m[1][2] = c1 * s2 * s3 - c3 * s1;

        m[2][0] = -s2;
        m[2][1] = c2 * s1;
        m[2][2] = c1 * c2;
    }


}
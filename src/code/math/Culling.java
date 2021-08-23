package code.math;

import java.nio.FloatBuffer;

/**
 *
 * @author Roman Lahin
 */
public class Culling {
    
    public static final int INVISIBLE = 0, VISIBLE = 1, FULLY_VISIBLE = 2;
    
    private static final float[][] frustum = new float[6][16];
	private static final float[] modl = new float[16], proj = new float[16], clip = new float[16];
    
    public static void set(FloatBuffer modlfb, FloatBuffer projfb) {
		modlfb.get(modl);
		modlfb.rewind();
		projfb.get(proj);
		projfb.rewind();

		/* Combine the two matrices (multiply projection by modelview) */
		clip[0] = modl[0] * proj[0] + modl[1] * proj[4] + modl[2] * proj[8] + modl[3] * proj[12];
		clip[1] = modl[0] * proj[1] + modl[1] * proj[5] + modl[2] * proj[9] + modl[3] * proj[13];
		clip[2] = modl[0] * proj[2] + modl[1] * proj[6] + modl[2] * proj[10] + modl[3] * proj[14];
		clip[3] = modl[0] * proj[3] + modl[1] * proj[7] + modl[2] * proj[11] + modl[3] * proj[15];

		clip[4] = modl[4] * proj[0] + modl[5] * proj[4] + modl[6] * proj[8] + modl[7] * proj[12];
		clip[5] = modl[4] * proj[1] + modl[5] * proj[5] + modl[6] * proj[9] + modl[7] * proj[13];
		clip[6] = modl[4] * proj[2] + modl[5] * proj[6] + modl[6] * proj[10] + modl[7] * proj[14];
		clip[7] = modl[4] * proj[3] + modl[5] * proj[7] + modl[6] * proj[11] + modl[7] * proj[15];

		clip[8] = modl[8] * proj[0] + modl[9] * proj[4] + modl[10] * proj[8] + modl[11] * proj[12];
		clip[9] = modl[8] * proj[1] + modl[9] * proj[5] + modl[10] * proj[9] + modl[11] * proj[13];
		clip[10] = modl[8] * proj[2] + modl[9] * proj[6] + modl[10] * proj[10] + modl[11] * proj[14];
		clip[11] = modl[8] * proj[3] + modl[9] * proj[7] + modl[10] * proj[11] + modl[11] * proj[15];

		clip[12] = modl[12] * proj[0] + modl[13] * proj[4] + modl[14] * proj[8] + modl[15] * proj[12];
		clip[13] = modl[12] * proj[1] + modl[13] * proj[5] + modl[14] * proj[9] + modl[15] * proj[13];
		clip[14] = modl[12] * proj[2] + modl[13] * proj[6] + modl[14] * proj[10] + modl[15] * proj[14];
		clip[15] = modl[12] * proj[3] + modl[13] * proj[7] + modl[14] * proj[11] + modl[15] * proj[15];

		/* Extract the numbers for the RIGHT plane */
		frustum[0][0] = clip[3] - clip[0];
		frustum[0][1] = clip[7] - clip[4];
		frustum[0][2] = clip[11] - clip[8];
		frustum[0][3] = clip[15] - clip[12];

		/* Normalize the result */
		float t = (float) Math.sqrt(
				frustum[0][0] * frustum[0][0] + frustum[0][1] * frustum[0][1] + frustum[0][2] * frustum[0][2]);
		frustum[0][0] /= t;
		frustum[0][1] /= t;
		frustum[0][2] /= t;
		frustum[0][3] /= t;

		/* Extract the numbers for the LEFT plane */
		frustum[1][0] = clip[3] + clip[0];
		frustum[1][1] = clip[7] + clip[4];
		frustum[1][2] = clip[11] + clip[8];
		frustum[1][3] = clip[15] + clip[12];

		/* Normalize the result */
		t = (float) Math.sqrt(
				frustum[1][0] * frustum[1][0] + frustum[1][1] * frustum[1][1] + frustum[1][2] * frustum[1][2]);
		frustum[1][0] /= t;
		frustum[1][1] /= t;
		frustum[1][2] /= t;
		frustum[1][3] /= t;

		/* Extract the BOTTOM plane */
		frustum[2][0] = clip[3] + clip[1];
		frustum[2][1] = clip[7] + clip[5];
		frustum[2][2] = clip[11] + clip[9];
		frustum[2][3] = clip[15] + clip[13];

		/* Normalize the result */
		t = (float) Math.sqrt(
				frustum[2][0] * frustum[2][0] + frustum[2][1] * frustum[2][1] + frustum[2][2] * frustum[2][2]);
		frustum[2][0] /= t;
		frustum[2][1] /= t;
		frustum[2][2] /= t;
		frustum[2][3] /= t;

		/* Extract the TOP plane */
		frustum[3][0] = clip[3] - clip[1];
		frustum[3][1] = clip[7] - clip[5];
		frustum[3][2] = clip[11] - clip[9];
		frustum[3][3] = clip[15] - clip[13];

		/* Normalize the result */
		t = (float) Math.sqrt(
				frustum[3][0] * frustum[3][0] + frustum[3][1] * frustum[3][1] + frustum[3][2] * frustum[3][2]);
		frustum[3][0] /= t;
		frustum[3][1] /= t;
		frustum[3][2] /= t;
		frustum[3][3] /= t;

		/* Extract the FAR plane */
		frustum[4][0] = clip[3] - clip[2];
		frustum[4][1] = clip[7] - clip[6];
		frustum[4][2] = clip[11] - clip[10];
		frustum[4][3] = clip[15] - clip[14];

		/* Normalize the result */
		t = (float) Math.sqrt(
				frustum[4][0] * frustum[4][0] + frustum[4][1] * frustum[4][1] + frustum[4][2] * frustum[4][2]);
		frustum[4][0] /= t;
		frustum[4][1] /= t;
		frustum[4][2] /= t;
		frustum[4][3] /= t;

		/* Extract the NEAR plane */
		frustum[5][0] = clip[3] + clip[2];
		frustum[5][1] = clip[7] + clip[6];
		frustum[5][2] = clip[11] + clip[10];
		frustum[5][3] = clip[15] + clip[14];

		/* Normalize the result */
		t = (float) Math.sqrt(
				frustum[5][0] * frustum[5][0] + frustum[5][1] * frustum[5][1] + frustum[5][2] * frustum[5][2]);
		frustum[5][0] /= t;
		frustum[5][1] /= t;
		frustum[5][2] /= t;
		frustum[5][3] /= t;
    }
	
	private float[] min, max;
    
    public Culling() {
        min = new float[3];
        max = new float[3];
    }
    
    public void setBox(float x1, float y1, float z1, float x2, float y2, float z2) {
        min[0] = x1; min[1] = y1; min[2] = z1;
        max[0] = x2; max[1] = y2; max[2] = z2;
    }
    
    public void setBox(Vector3D minv, Vector3D maxv) {
        min[0] = minv.x; min[1] = minv.y; min[2] = minv.z;
        max[0] = maxv.x; max[1] = maxv.y; max[2] = maxv.z;
    }
    
    public int visible() {
		//тестируем 6 плоскостей фрустума
		float minx = min[0], miny = min[1], minz = min[2];
		float maxx = max[0], maxy = max[1], maxz = max[2];
		
		for(int i = 0; i < 6; i++) {
			//находим ближайшую к плоскости вершину
			//проверяем, если она находится за плоскостью, то объект вне врустума
			
			float[] frustum = Culling.frustum[i];
			float frustum0 = frustum[0],
					frustum1 = frustum[1],
					frustum2 = frustum[2];
			
			float minxf = minx * frustum0,
					maxxf = maxx * frustum0,
					minyf = miny * frustum1,
					maxyf = maxy * frustum1,
					minzf = minz * frustum2,
					maxzf = maxz * frustum2;
			
			float d = (minxf > maxxf ? minxf : maxxf)
					+ (minyf > maxyf ? minyf : maxyf)
					+ (minzf > maxzf ? minzf : maxzf)
					+ frustum[3];
			
			if(d <= 0) return INVISIBLE;
		}
		
		//если не нашли разделяющей плоскости, считаем объект видим
		return VISIBLE;
    }
    
}

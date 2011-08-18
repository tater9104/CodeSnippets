package com.cwd.demo.Tools;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public class BitmapScaler {
	
	private Bitmap scaledBitmap;
	
	public Bitmap getScaledBitmap()
	{
		return scaledBitmap;
	}
	
	/*  IMPORANT NOTES:
	 * The process of scaling bitmaps for Android is not a straightforward process.  In order to 
	 * help preserve memory on the phone, you need to go through several steps:
	 *  
	 *  1.  Decode the target resource, but use the inJustDecodeBounds option.  This allows you to "sample" 
	 *  the resource without actually incurring the hit of loading the entire resource.  If set to true, the decoder will return null 
	 *  (no bitmap), but the out... fields will still be set, allowing the caller to query the bitmap without having to allocate 
	 *  the memory for its pixels.
	 *  2.  Determine the new aspects of your bitmap, particularly the scale and sample.  If the sample size is set to a value > 1, 
	 *  requests the decoder to subsample the original image, returning a smaller image to save memory. The sample size is the number 
	 *  of pixels in either dimension that correspond to a single pixel in the decoded bitmap. For example, inSampleSize == 4 returns 
	 *  an image that is 1/4 the width/height of the original, and 1/16 the number of pixels. Any value <= 1 is treated the same as 1. 
	 *  Note: the decoder will try to fulfill this request, but the resulting bitmap may have different dimensions that precisely what 
	 *  has been requested. Also, powers of 2 are often faster/easier for the decoder to honor.
	 *  3.  Prescale the bitmap as much as possible, rather than trying to fully decode it in memory.  This is a less
	 *  expensive operation and allows you to "right size" your image.
	 *  4.  Create your new bitmap, applying a matrix to "fine tune" the final resize.
	 *  
	 *  Partial Ref:  http://zerocredibility.wordpress.com/2011/01/27/android-bitmap-scaling/
	 */
	
	public BitmapScaler(Resources resources, int targetResourceID, int targetWidth, int targetHeight)
	{		
		BitmapInfo originalInfo = getOriginalBitmapInfo(resources, targetResourceID);
		BitmapInfo newInfo = getScaledBitmapInfo(targetHeight, targetWidth, originalInfo);
		prescaleScaledBitmap(resources, targetResourceID, newInfo);
		scaleScaledBitmap(newInfo);
	}
	
	private void scaleScaledBitmap(BitmapInfo newInfo)
	{
		
		int ScaledHeight = scaledBitmap.getHeight();
		int ScaledWidth = scaledBitmap.getWidth();
		
		float MatrixWidth = ((float)newInfo.width) / ScaledWidth;
		float MatrixHeight = ((float)newInfo.height) / ScaledHeight;
		
		Matrix matrix = new Matrix();
		matrix.postScale(MatrixWidth, MatrixHeight);
		
		scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, ScaledWidth, ScaledHeight, matrix, true);
	}
	
	private void prescaleScaledBitmap(Resources resources, int targetResourceID, BitmapInfo newInfo)
	{
		BitmapFactory.Options scaledOpts = new BitmapFactory.Options();
		scaledOpts.inSampleSize = newInfo.sample;
		scaledBitmap = BitmapFactory.decodeResource(resources, targetResourceID, scaledOpts);		
	}
		
	private BitmapInfo getOriginalBitmapInfo(Resources resources, int targetResourceID)
	{
		BitmapFactory.Options bitOptions = new BitmapFactory.Options();
		bitOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(resources, targetResourceID, bitOptions);
		
		return new BitmapInfo(bitOptions.outHeight,bitOptions.outWidth);
	}
	
	private BitmapInfo getScaledBitmapInfo(int targetHeight, int targetWidth, BitmapInfo originalBitmapInfo)
	{
		float HeightRatio = targetHeight / (float)originalBitmapInfo.height;
		float WidthRatio = targetWidth / (float)originalBitmapInfo.width;
		
		BitmapInfo newInfo = new BitmapInfo(0,0);
		
		if (HeightRatio > WidthRatio)
		{
			newInfo.scale = WidthRatio;
			newInfo.width = targetWidth;
			newInfo.height = (int)(newInfo.scale * originalBitmapInfo.height);
		} else {
			newInfo.scale = HeightRatio;
			newInfo.height = targetHeight;
			newInfo.width = (int)(newInfo.scale * originalBitmapInfo.width);
		}
		
		newInfo.sample = 1;
		
		int SampleHeight = originalBitmapInfo.height;
		int SampleWidth = originalBitmapInfo.width;
		
		while (true) {
			if (SampleWidth / 2 < newInfo.width || SampleHeight / 2 < newInfo.height) {
				break;
			}
			SampleWidth /= 2;
			SampleHeight /= 2;
			newInfo.sample *= 2;
		}
		
		return newInfo;
	}
	
}

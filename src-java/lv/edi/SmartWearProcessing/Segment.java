package lv.edi.SmartWearProcessing;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
/**
 * class that describes one sensor grid segment (node or accelerometer)
 * @author Richards
 *
 */
public class Segment {
	/**
	 * initial cross point coordinates (before vertical axis compensation)
	 * 
	 * 				P0
	 *              |
	 *              |
	 *    p3________|center_____P1
	 *             	|
	 *              |
	 *              |
	 *              p2
	 *              
	 *              iicross[0]=p0
	 *              iicross[1]=p1
	 *              iicross[2]=p2
	 *              iicross[3]=p3
	 */ 
	public float[][] iicross={{0,0, (float)2.25},{(float)1.75,0,0},{0,0,(float)-2.25},{(float)-1.75,0,0}}; // initial cross consisting of 4 vectors depends on distance from sensors
	/**
	 * initial cross after rotation around vertical axis compensation
	 */
	public float[][] initialCross={{0,0, (float)2.25},{(float)1.75,0,0},{0,0,(float)-2.25},{(float)-1.75,0,0}}; // initial cross consisting of 4 vectors depends on distance from sensors
	/**
	 * cross that is oriented as sensor (represents real time orientation)
	 */
	public float[][] cross = new float[4][3];			   // rotated cross
	/**
	 * center coordinate of sensor
	 */
	public float[] center={0,0,0};				   // center coordinates of segment
	/**
	 * constructor
	 * @param angle angle that represents initial rotation around Z axis
	 */
	
	public Segment(){
		
	}
	public Segment(float angle){
		float[] q = new float[4];
		float[] n = {0, 0, 1};
		float tempCross[][] = new float[4][3];
		for(int i=0;i<4;i++){
			for(int j=0;j<3;j++){
				tempCross[i][j]=initialCross[i][j];
			}
		}
		SensorDataProcessing.quaternion(n, angle, q);
		for(int i = 0; i<4; i++){
			SensorDataProcessing.quatRotate(q, tempCross[i], initialCross[i]);
			//Log.d("SETTING INITIAL CROSS", " "+i+": "+initialCross[i][0]+" "+ initialCross[i][1]+" "+initialCross[i][2]);
		}	
	}
	/**Sets segment current orientation 
	 * @param sensor sensor of corresponding sensor
	 * */
	public void setSegmentOrientation(Sensor sensor){
		float[] n = new float[3]; // rotation axis
		float[] vec = {0, 0, 1};  // Z axis
		float fi = 0; 			  // rotation angle
		float[] q = new float[4];// quaternion
		
		SensorDataProcessing.crossProduct(sensor.getAccNorm(),vec, n); // rotation axis
		SensorDataProcessing.normalizeVector(n);					      // normalize rotation axis				
		fi = (float)Math.acos(SensorDataProcessing.dotProduct(vec, sensor.getAccNorm())); // get rotation angle
		SensorDataProcessing.quaternion(n, fi, q); //get quaternion
		for(int i = 0; i<4; i++){
			SensorDataProcessing.quatRotate(q, initialCross[i], cross[i]);
			//Log.d("SETTING INITIAL CROSS", " "+i+": "+initialCross[i][0]+" "+ initialCross[i][1]+" "+initialCross[i][2]);
			
		}
	}
	
	public static void setAllSegmentOrientations(Segment[][] segmentArray,Sensor[][] sensorGrid){
		for(int i=0; i<segmentArray.length; i++){
			for(int j=0; j<segmentArray[0].length; j++){
				segmentArray[i][j].setSegmentOrientation(sensorGrid[i][j]);
			}
		}
	}
	/**
	 * method sets segment rotation around vertical axis
	 * @param angle - rotation angle in radians
	 */
	public void setSegmentZRotation(float angle){
		float[] q = new float[4];
		float[] n = {0, 0, 1};
		SensorDataProcessing.quaternion(n, angle, q);
		float[][] tempCross = new float[4][3];
		for(int i=0;i<4;i++){
			for(int j=0;j<3;j++){
				tempCross[i][j]=iicross[i][j];
			}
		}
		for(int i=0; i<4; i++){
			SensorDataProcessing.quatRotate(q, tempCross[i], initialCross[i]);
		}
	}
	/** sets initial cross vectors @args verticDistance - vertical distance between sensors horizDistance - horizontal ditances between sensors*/
	public void setInitialCross(float verticDistance, float horizDistance){
		initialCross[0][0]=0;
		initialCross[0][1]=0;
		initialCross[0][2]=verticDistance/2;
		
		initialCross[1][0]=0;
		initialCross[1][1]=horizDistance/2;
		initialCross[1][2]=0;
		
		initialCross[2][0]=0;
		initialCross[2][1]=0;
		initialCross[2][2]=-verticDistance/2;
		
		initialCross[3][0]=0;
		initialCross[3][1]=-horizDistance/2;
		initialCross[3][2]=0;
	}
	/**method sets segment center coordinates for segment grid @args segmentArray is array of segments , referenceRow
	 * is row index for accelerometer from which to draw, referenceColumns is column index for accelerometer from which 
	 * to draw*/
	public static void setSegmentCenters(Segment[][] segmentArray, short referenceRow, short referenceCol){
		// calculating row
		short NR_ROWS = (short)segmentArray.length;
		short NR_COLS = (short)segmentArray[0].length;
		short toBottom=referenceRow;
		while(toBottom>0){
			toBottom--;
			segmentArray[toBottom][referenceCol].center[0]=segmentArray[toBottom+1][referenceCol].center[0]+
														 segmentArray[toBottom+1][referenceCol].cross[2][0]+
														 segmentArray[toBottom][referenceCol].cross[2][0]; // X coordinate for segment center
			segmentArray[toBottom][referenceCol].center[1]=segmentArray[toBottom+1][referenceCol].center[1]+
					 									 segmentArray[toBottom+1][referenceCol].cross[2][1]+
					 									 segmentArray[toBottom][referenceCol].cross[2][1]; // Y coordinate for segment center
			segmentArray[toBottom][referenceCol].center[2]=segmentArray[toBottom+1][referenceCol].center[2]+
														 segmentArray[toBottom+1][referenceCol].cross[2][2]+
														 segmentArray[toBottom][referenceCol].cross[2][2]; // Z coordinate for segment center
		}
		short toTop=referenceRow;
		while(toTop<(NR_ROWS-1)){
			toTop++;
			segmentArray[toTop][referenceCol].center[0]=segmentArray[toTop-1][referenceCol].center[0]+
														  segmentArray[toTop-1][referenceCol].cross[0][0]+
														  segmentArray[toTop][referenceCol].cross[0][0];// X coordinate for segment center		
			segmentArray[toTop][referenceCol].center[1]=segmentArray[toTop-1][referenceCol].center[1]+
					 									  segmentArray[toTop-1][referenceCol].cross[0][1]+
					 									  segmentArray[toTop][referenceCol].cross[0][1];// Y coordinate for segment center
			segmentArray[toTop][referenceCol].center[2]=segmentArray[toTop-1][referenceCol].center[2]+
														  segmentArray[toTop-1][referenceCol].cross[0][2]+
														  segmentArray[toTop][referenceCol].cross[0][2];// Z coordinate for segment center											 
		}
		// calculating centers for rest of the segments
		
		for(int i=0;i<NR_ROWS;i++){
			short toLeft=referenceCol;
			while(toLeft>0){
				toLeft--;
				segmentArray[i][toLeft].center[0]=segmentArray[i][toLeft+1].center[0]+
													segmentArray[i][toLeft+1].cross[3][0]+
													segmentArray[i][toLeft].cross[3][0]; // X coordinate
				segmentArray[i][toLeft].center[1]=segmentArray[i][toLeft+1].center[1]+
													segmentArray[i][toLeft+1].cross[3][1]+
													segmentArray[i][toLeft].cross[3][1]; // Y coordinate
				segmentArray[i][toLeft].center[2]=segmentArray[i][toLeft+1].center[2]+
													segmentArray[i][toLeft+1].cross[3][2]+
													segmentArray[i][toLeft].cross[3][2]; // Z coordinate
			}
			short toRight=referenceCol;
			while(toRight<(NR_COLS-1)){
				toRight++;
				segmentArray[i][toRight].center[0]=segmentArray[i][toRight-1].center[0]+
												 segmentArray[i][toRight-1].cross[1][0]+
												 segmentArray[i][toRight].cross[1][0]; // X coordinate
				segmentArray[i][toRight].center[1]=segmentArray[i][toRight-1].center[1]+
						 						 segmentArray[i][toRight-1].cross[1][1]+
						 						 segmentArray[i][toRight].cross[1][1]; // Y coordinate
				segmentArray[i][toRight].center[2]=segmentArray[i][toRight-1].center[2]+
						 						 segmentArray[i][toRight-1].cross[1][2]+
						 						 segmentArray[i][toRight].cross[1][2]; // X coordinate
			}
		}
	
		
	}
	/**
	 * method that returns X coordinate of segment centre
	 * @return X coordinate of sement center
	 */
	public synchronized float getSegmentCenterX(){
		return center[0];
	}
	/**
	 *  return Y coordinate of segment centre
	 * @return Y coordinate of segment centre
	 */
	public synchronized float getSegmentCenterY(){
		return center[1];
	}
	/**
	 * returns Z coordinate of segment centre
	 * @return Y coordinate of segment centre
	 */
	public synchronized float getSegmentCenterZ(){
		return center[2];
	}
	/**
	 * method that gives particular vector of segment
	 * @param nr segment number
	 * @return cross vector float[3]
	 */
	public float [] getCross(int nr){
		float[] crossVec = new float[3];
		for(int i=0;i<3;i++){
			crossVec[i]=cross[nr][i]; // copy cross array to cross vec
		}
		return crossVec;
	}
	/**
	 * method that sets centres of segment array to default
	 * @param segmentArray input array of Segments
	 */
	public static void setDefaultCenters(Segment[][] segmentArray){
		for(int i=0;i<segmentArray.length;i++){
			for(int j=0;j<segmentArray[0].length;j++){
				segmentArray[i][j].center[2]=5*i;
				segmentArray[i][j].center[0]=5*j;
			}
		}
	}
	/**
	 * method that compares two segment arrays by distance between segment centres
	 * @param referenceState - first state Segment[][] array
	 * @param currentState - second state Segment[][] array
	 * @return array of distances[][]
	 */
	public static float[][] compareByCenterDistances(Segment[][] referenceState, Segment[][] currentState){
		float[][] distances=new float[referenceState.length][referenceState[0].length];
		for(int i=0;i<referenceState.length;i++){
			for(int j=0;j<referenceState[0].length;j++){
				distances[i][j]=(float)sqrt(pow(referenceState[i][j].center[0]-currentState[i][j].center[0],2)+
											pow(referenceState[i][j].center[1]-currentState[i][j].center[1],2)+
											pow(referenceState[i][j].center[2]-currentState[i][j].center[2],2));
			}
		}
		return distances;
	}
	/**method that compares two segment arrays by distances between segment centres in sagital plane
	 * 
	 *@param referenceState - first state Segment[][] array
	 * @param currentState - second state Segment[][] array
	 * @return array of distances[][]
	 */
	public static float[][] compareByDistancesSagital(Segment[][] referenceState, Segment[][] currentState){
		float[][] distancesSagital = new float[referenceState.length][referenceState[0].length];
		for(int i=0;i<referenceState.length;i++){
			for(int j=0;j<referenceState[0].length;j++){
				distancesSagital[i][j]=(float)sqrt(pow(referenceState[i][j].center[1]-currentState[i][j].center[1],2)+
											       pow(referenceState[i][j].center[2]-currentState[i][j].center[2],2));
			}
		}
		return distancesSagital;
	}
	/**method that compares two segment arrays by distances between segment centres in coronal plane
	 * 
	 *@param referenceState - first state Segment[][] array
	 * @param currentState - second state Segment[][] array
	 * @return array of distances[][]
	 */
	public static float[][] compareByDistancesCoronal(Segment[][] referenceState, Segment[][] currentState){
		float[][] distancesCoronal = new float[referenceState.length][referenceState[0].length];
		for(int i=0;i<referenceState.length;i++){
			for(int j=0;j<referenceState[0].length;j++){
				distancesCoronal[i][j]=(float)sqrt(pow(referenceState[i][j].center[0]-currentState[i][j].center[0],2)+
											       pow(referenceState[i][j].center[2]-currentState[i][j].center[2],2));
			}
		}
		return distancesCoronal;
	}
	/**
	 * method for compensating for tilt 
	 * @param refferenceStateInitial
	 * @param currentState
	 * @param refferenceState
	 * @param referenceRow
	 * @param referenceCol
	 */
	public static void compansateCentersForTilt(Segment[][] refferenceStateInitial, Segment[][] currentState, Segment[][] refferenceState, int referenceRow, int referenceCol){
		float n[] = new float[3];
		float fi;
		float[] q = new float[4];
		SensorDataProcessing.crossProduct(refferenceStateInitial[referenceRow+1][referenceCol].center, currentState[referenceRow+1][referenceCol].center, n);
		//Log.d("COMPENSATE_TILT","n: "+n[0]+" "+n[1]+" "+n[2]);
		SensorDataProcessing.normalizeVector(n);
		//Log.d("COMPENSATE_TILT","n_N: "+n[0]+" "+n[1]+" "+n[2]);
		fi=(float)Math.acos(SensorDataProcessing.dotProduct(refferenceStateInitial[referenceRow+1][referenceCol].center, currentState[referenceRow+1][referenceCol].center)/
				(SensorDataProcessing.getVectorLength(refferenceStateInitial[referenceRow+1][referenceCol].center)*SensorDataProcessing.getVectorLength(currentState[referenceRow+1][referenceCol].center)));
		SensorDataProcessing.quaternion(n, fi, q);
		for(int i=0; i<refferenceState.length; i++){
			for(int j=0; j<refferenceState[0].length;j++){
				SensorDataProcessing.quatRotate(q, refferenceStateInitial[i][j].center, refferenceState[i][j].center);
				//Log.d("COMPENSATE_TILT","rotated center: "+refferenceState[i][j].center[0]+" "+refferenceState[i][j].center[1]+" "+refferenceState[i][j].center[2]);
			}
		}
	}
}

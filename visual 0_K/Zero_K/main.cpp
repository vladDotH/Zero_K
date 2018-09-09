#include "Zero_K.h"

#include "VisualFunction.h"

int main()
{
    VideoCapture capture( 0 );

    if( !capture.isOpened() )
    {
        return -1;
    }
	
    namedWindow( "cam" );
	namedWindow("HSVimage");

	namedWindow("draw");

	VisualFunction func(CvSize( 700, 700 ));
	
	setMouseCallback("draw", VisualFunction::getCallBack, (void*)&func);

    setMouseCallback( "cam", MainCallBack );

    namedWindow( "color detecting" );
    createTrackbar( "hue", "color detecting", &HSV.hueDiff, 255, barBack );
    createTrackbar( "satur.", "color detecting", &HSV.MIN.saturation, 255, barBack );
    createTrackbar( "brigt.", "color detecting", &HSV.MIN.brigthness, 255, barBack );

    namedWindow( "borders" );
    createTrackbar( "high", "borders", &frameBegin.y, imgSize.y, barBack );
    createTrackbar( "left", "borders", &frameBegin.x, imgSize.x, barBack );
    createTrackbar( "down", "borders", &frameEnd.y, imgSize.y, barBack );
    createTrackbar( "right", "borders", &frameEnd.x, imgSize.x, barBack );

    namedWindow( "robot control" );
    createTrackbar( "proportional", "robot control", &robot.RIDE_COEFFS.prop, 100, barBack );
    createTrackbar( "cubic", "robot control", &robot.RIDE_COEFFS.cube, 100, barBack );
    createTrackbar( "integral", "robot control", &robot.RIDE_COEFFS.integral, 100, barBack );
    createTrackbar( "differencial", "robot control", &robot.RIDE_COEFFS.differencial, 100, barBack );

	bool p = true;
		  
    while( true )
    {
        if( !capture.read( frame ) )
            continue;

        resize( frame, RGBimage, imgSize );

        GaussianBlur( RGBimage, RGBimage, Size( 3, 3 ), 0 );

        cvtColor( RGBimage, HSVimage, CV_BGR2HSV );

        robot.refuse();
        ball.refuse();

        for ( int w = frameBegin.x; w < frameEnd.x; w++ )
        {
            for ( int h = frameBegin.y; h < frameEnd.y; h++ )
            {
                Color pixel = read( HSVimage, Point( w, h ) );

                if( eqPix( robot.getColor(), pixel, HSV.hueDiff, HSV.MIN.saturation, HSV.MIN.brigthness ) )
                {
                    robot.addpixel( w, h );

                    if( highLightning )
                        write( RGBimage, Point( w, h ), Color( 150, 20, 120 ) );
                }

                if( eqPix( ball.getColor(), pixel, HSV.hueDiff, HSV.MIN.saturation, HSV.MIN.brigthness ) )
                {
                    ball.addpixel( w, h );

                    if( highLightning )
                        write( RGBimage, Point( w, h ), Color( 150, 100, 0 ) );
                }

            }
        }

        robot.detect();
        ball.detect();

        if( markCenter )
        {
            circle( RGBimage, robot.getPosition(), 6, Scalar( 0, 0, 0 ), -1 );
            circle( RGBimage, ball.getPosition(), 6, Scalar( 255, 255, 255 ), -1 );
        }

        if( showFrame )
        {
            rectangle( RGBimage, frameBegin, frameEnd, Scalar( 0, 0, 150 ), 3 );
        }

        if( robot.getMode() )
        {
            if( KEY_DOWN( VK_LEFT ) )
                robot.move( 100 );
            else if( KEY_DOWN( VK_RIGHT ) )
                robot.move( -100 );
            else if( KEY_DOWN( VK_UP ) )
                robot.kick();
            else
                robot.move( 0 );
        }

        else
        {
            Point difference = robot.getPosition() - ball.getPosition();

            if( abs( difference.y ) < 30 )
                robot.kick();

            int speed = difference.x * robot.RIDE_COEFFS.prop                         
                + difference.x * difference.x * difference.x * robot.RIDE_COEFFS.cube;
        }

        imshow( "cam", RGBimage );
		imshow("HSVimage", HSVimage);
		imshow("draw", func.getImage());

        char key = waitKey( 5 );

        if( key == VK_ESCAPE )
            break;

        if( key == 'h' )
            highLightning = !highLightning;

        if( key == 'c' )
            markCenter = !markCenter;

        if( key == 'b' )
            showFrame = !showFrame;

        if( key == 'a' )
            robot.switchMode();

        if( key == 't' )
            robot.switchDirection();

        if( key == 'g' )
            metrical = metrical_generator( frameBegin, frameEnd );

		if (key == 'r') {
			robot.setColor(Color(-999, -999, -999));
			ball.setColor(Color(-999, -999, -999));
		}

		if (key == 'f')
			cout << func(50) << endl;

    }

    return 0;
}
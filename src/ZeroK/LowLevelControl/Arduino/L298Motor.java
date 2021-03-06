package ZeroK.LowLevelControl.Arduino;

@Deprecated
public class L298Motor {

    protected Arduino controller;

    private int speedPin, dirPin1, dirPin2;
    private int speed;

    public L298Motor(int speedPin, int dirPin1, int dirPin2) {
        this.speedPin = speedPin;
        this.dirPin1 = dirPin1;
        this.dirPin2 = dirPin2;
    }

    public L298Motor(Arduino controller, int speedPin, int dirPin1, int dirPin2) {
        this(speedPin, dirPin1, dirPin2);
        attachToArduino(controller);
    }

    public void attachToArduino(Arduino controller) {
        this.controller = controller;
    }

    public void move(int speed) {
        if (speed > 255) speed = 255;
        if (speed < -255) speed = -255;

        if (this.speed == speed)
            return;

        this.speed = speed;
        if (speed == 0) {
            controller.digitalWrite(dirPin1, Arduino.Mode.LOW);
            controller.digitalWrite(dirPin2, Arduino.Mode.LOW);
        } else if (speed > 0) {
            controller.digitalWrite(dirPin1, Arduino.Mode.HIGH);
            controller.digitalWrite(dirPin2, Arduino.Mode.LOW);
        } else {
            controller.digitalWrite(dirPin1, Arduino.Mode.LOW);
            controller.digitalWrite(dirPin2, Arduino.Mode.HIGH);
        }

        controller.analogWrite(speedPin, Math.abs(speed));
    }
}

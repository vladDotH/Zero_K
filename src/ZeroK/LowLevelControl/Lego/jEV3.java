package ZeroK.LowLevelControl.Lego;

import ZeroK.LowLevelControl.SafeSerialPort;
import jssc.*;

public class jEV3 implements SerialPortEventListener, AutoCloseable {
    private static class MessageType {
        static class Mode {
            public byte first, second;
        }

        static Mode speed, stop, start;

        static {
            speed = new Mode();
            speed.first = 10;
            speed.second = -91;

            start = new Mode();
            start.first = 8;
            start.second = -90;

            stop = new Mode();
            stop.first = 9;
            stop.second = -93;
        }
    }

    private static class MotorCodes {
        static byte A = 0b1,
                B = 0b10,
                C = 0b100,
                D = 0b1000;
    }

    private static class Stop {
        static byte Float = 0,
                Break = 1;
    }

    public class Motor {
        private byte code;
        private byte speed = 0;
        private boolean active = false;

        private Motor(int code) {
            this.code = (byte) code;
        }

        public void setSpeed(int speed) {
            if (speed != this.speed)
                jEV3.this.setSpeed(code, speed);
        }

        public void start() {
            if (!active) {
                jEV3.this.start(code);
                active = true;
            }
        }

        public void stopFloat() {
            if (active) {
                jEV3.this.stop(code, Stop.Float);
                active = false;
            }
        }

        public void stopBreak() {
            if (active) {
                jEV3.this.stop(code, Stop.Break);
                active = false;
            }
        }
    }

    public final Motor A = new Motor(MotorCodes.A),
            B = new Motor(MotorCodes.B),
            C = new Motor(MotorCodes.C),
            D = new Motor(MotorCodes.D);

    private SafeSerialPort ev3;
    private Motor left = B, right = C;

    public jEV3(String portName) {
        ev3 = new SafeSerialPort(portName);

        try {
            ev3.openPort();

            ev3.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            ev3.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |
                    SerialPort.FLOWCONTROL_RTSCTS_OUT);

            ev3.addEventListener(this, SerialPort.MASK_RXCHAR);

            Thread.sleep(50);

            System.out.println("Ev3 at port " + portName + " connected");

        } catch (SerialPortException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public void setLR(Motor left, Motor right) {
        this.left = left;
        this.right = right;
    }

    public void ride(int left, int right) {
        this.left.setSpeed(left);
        this.right.setSpeed(right);

        this.left.start();
        this.right.start();

        if (left == 0)
            this.left.stopFloat();
        if (right == 0)
            this.right.stopFloat();
    }

    private void setSpeed(byte motor, int speed) {
        byte[] message = new byte[12];
        byte[] defaultPack = packMessage(MessageType.speed);

        System.arraycopy(defaultPack, 0, message, 0, defaultPack.length);

        message[9] = motor;
        message[10] = -127;
        message[11] = (byte) speed;

        send(message);
    }

    private void start(byte motor) {
        byte[] message = new byte[10];
        byte[] defaultPack = packMessage(MessageType.start);

        System.arraycopy(defaultPack, 0, message, 0, defaultPack.length);

        message[9] = motor;

        send(message);
    }

    private void stop(byte motor, byte stopMode) {
        byte[] message = new byte[11];
        byte[] defaultPack = packMessage(MessageType.stop);

        System.arraycopy(defaultPack, 0, message, 0, defaultPack.length);

        message[9] = motor;
        message[10] = stopMode;

        send(message);
    }

    private byte[] packMessage(MessageType.Mode mode) {
        byte[] msg = new byte[9];
        msg[0] = mode.first;
        msg[7] = mode.second;
        msg[4] = -128;

        return msg;
    }

    private void send(byte[] message) {
        ev3.writeBytes(message);
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {

    }

    @Override
    public void close() {
        try {
            ev3.closePort();
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }
}

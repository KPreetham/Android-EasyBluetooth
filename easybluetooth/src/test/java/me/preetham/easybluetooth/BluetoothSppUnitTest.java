package me.preetham.easybluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import me.preetham.easybluetooth.enums.Mode;
import me.preetham.easybluetooth.interfaces.SppConnectionListener;
import me.preetham.easybluetooth.interfaces.SppDataListener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BluetoothSppUnitTest {
    private final String DATA_SENT = "hello world";

    @Mock
    BluetoothDevice mock_bluetooth_device;
    @Mock
    BluetoothSocket mock_bluetooth_socket;
    @Mock
    InputStream mock_inputStream;
    @Mock
    OutputStream mock_outputStream;

    private int BUFFER_SIZE = 1024;
    private Mode MODE = Mode.INSECURE;
    private UUID uuid = UUID.randomUUID();

    @Mock
    private BluetoothSpp mock_bluetoothSpp;
    @Mock
    private SppDataListener mock_dataListener;
    @Mock
    private SppConnectionListener mock_connectionListener;

    @Before
    public void initStuffs() {
        setup();
    }

    @Test
    public void testDummy() {
        assertTrue((1 == 1));
    }

    @Test
    public void test_bluetooth_spp_builder() {
        BluetoothSpp.Builder builder = get_dummy_bluetooth_spp_builder();

        assertEquals(builder.getUuid(), uuid);
        assertEquals(builder.getMode(), MODE);
        assertEquals(builder.getBufferSize(), BUFFER_SIZE);
    }

    @Test
    public void test_bluetooth_spp_connect() {
        BluetoothSpp.Builder builder = get_dummy_bluetooth_spp_builder();

        mock_bluetoothSpp = builder.build();

        mock_bluetoothSpp.connect(mock_bluetooth_device);
        mock_bluetoothSpp.write(DATA_SENT);
    }

    @Test
    public void test_bluetooth_spp_write_data() {

    }

    public BluetoothSpp.Builder get_dummy_bluetooth_spp_builder() {
        BluetoothSpp.Builder builder = new BluetoothSpp.Builder()
                .setBufferSize(BUFFER_SIZE)
                .setMode(MODE)
                .setUuid(uuid)
                .setDataListener(mock_dataListener)
                .setConnectionListener(mock_connectionListener);

        return builder;
    }

    private void setup() {
        try {
            setup_mock_bluetooth_device();
            setup_mock_bluetooth_socket();
            setup_mock_io_streams();
            setup_mock_data_listener();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setup_mock_io_streams() throws IOException {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                mock_dataListener.onDataReceived(DATA_SENT.getBytes(), DATA_SENT.length());
                return null;
            }
        }).when(mock_inputStream).read();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                String tmp = new String(invocation.getArgumentAt(0, byte[].class));
                assertEquals(tmp, DATA_SENT);
                return null;
            }
        }).when(mock_outputStream).write(DATA_SENT.getBytes());
    }

    private void setup_mock_bluetooth_socket() throws IOException {
        when(mock_bluetooth_socket.getInputStream()).thenReturn(mock_inputStream);
        when(mock_bluetooth_socket.getOutputStream()).thenReturn(mock_outputStream);
    }

    private void setup_mock_bluetooth_device() throws IOException {
        when(mock_bluetooth_device.createInsecureRfcommSocketToServiceRecord((UUID) any())).thenReturn(mock_bluetooth_socket);
    }

    private void setup_mock_data_listener() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {

                byte[] data = invocation.getArgumentAt(0, byte[].class);
                String data_received = new String(data);

                assertEquals(DATA_SENT, data_received);
                assertEquals(DATA_SENT.length(), data.length);

                return null;
            }
        }).when(mock_dataListener).onDataSent(any(byte[].class));
    }
}

package nl.vu.cs.cn;


import java.io.IOException;
import java.util.Random;

import nl.vu.cs.cn.IP.IpAddress;
import nl.vu.cs.cn.util.util;
/**
 * This class represents a TCP stack. It should be built on top of the IP stack
 * which is bound to a given IP address.
 */
public class TCP {

	/** The underlying IP stack for this TCP stack. */
    private IP ip;

    /**
     * This class represents a TCP socket.
     *
     */
    public class Socket {


        final int MAX_ATTEMPTS=10;

    	/* Hint: You probably need some socket specific data. */

    	/**
    	 * Construct a client socket.
    	 */

        private TcpControlBlock tcb;

    	private Socket(short port) {

            tcb = new TcpControlBlock();
            tcb.tcb_our_ip_address = Integer.reverseBytes(ip.getLocalAddress().getAddress());
            tcb.tcb_our_port = port;
            tcb.tcb_state = TcpControlBlock.ConnectionState.CLOSED;


    	}


		/**
         * Connect this socket to the specified destination and port.
         *
         * @param dst the destination to connect to
         * @param port the port to connect to
         * @return true if the connect succeeded.
         */
        public boolean connect(IpAddress dst, short port) {

            // Implement the connection side of the three-way handshake here.
        	ip.getLocalAddress();

            if (tcb.tcb_state != TcpControlBlock.ConnectionState.CLOSED){
                return false;
            }

            tcb.tcb_their_ip_address = Integer.reverseBytes(dst.getAddress());
            tcb.tcb_their_port = port;
            tcb.tcb_state = TcpControlBlock.ConnectionState.SYN_SENT;
            int initialSeqNumber = new Random().nextInt();
            tcb.tcb_our_sequence_number = initialSeqNumber;
            tcb.tcb_our_expected_ack = initialSeqNumber + 1;
            byte [] emptyData = new byte[0];
            TCPSegment syn = new TCPSegment(tcb.tcb_our_port,port,initialSeqNumber , 0, util.SYN , emptyData);

            if (send(syn, util.SYNACK)){
                tcb.tcb_state = TcpControlBlock.ConnectionState.ESTABLISHED;
            }



            return false;
        }


        /**
         * Accept a connection on this socket.
         * This call blocks until a connection is made.
         */
        public void accept() {

            // Implement the receive side of the three-way handshake here.

        }

        /**
         * Reads bytes from the socket into the buffer.
         * This call is not required to return maxlen bytes
         * every time it returns.
         *
         * @param buf the buffer to read into
         * @param offset the offset to begin reading data into
         * @param maxlen the maximum number of bytes to read
         * @return the number of bytes read, or -1 if an error occurs.
         */
        public int read(byte[] buf, int offset, int maxlen) {

            // Read from the socket here.

            return -1;
        }

        /**
         * Writes to the socket from the buffer.
         *
         * @param buf the buffer to
         * @param offset the offset to begin writing data from
         * @param len the number of bytes to write
         * @return the number of bytes written or -1 if an error occurs.
         */
        public int write(byte[] buf, int offset, int len) {

            // Write to the socket here.

            return -1;
        }

        /**
         * Closes the connection for this socket.
         * Blocks until the connection is closed.
         *
         * @return true unless no connection was open.
         */
        public boolean close() {

            // Close the socket cleanly here.

            return false;
        }

        private boolean send(TCPSegment segment, int expectedFlags){

            int attempts = 0;

            while (attempts < MAX_ATTEMPTS) {
                try {
                    sendSegment(segment, tcb);
                    try {
                        TCPSegment receivedSegment = receiveSegment();
                        if (receivedSegment.is(expectedFlags) && receivedSegment.isValid(tcb)){
                            return true;
                        }else{
                            attempts++;
                        }

                    }catch(Exception e){
                        attempts++;
                    }

                } catch (IOException e) {
                    attempts++;
                }
            }

            return false;
        }

    }

    private int sendSegment(TCPSegment tcpSeg,TcpControlBlock tcb) throws IOException{

        int dstAddress = Integer.reverse(tcb.tcb_their_ip_address);
        int packetID = new Random().nextInt();
        byte[] data = new byte[tcpSeg.length()];
        tcpSeg.toArray(data, 0);
        IP.Packet pck = new IP.Packet(dstAddress,IP.TCP_PROTOCOL,packetID,data,tcpSeg.length());


        return ip.ip_send(pck);

    }

    private TCPSegment receiveSegment() throws IOException , InterruptedException{

        IP.Packet pck = new IP.Packet();
        ip.ip_receive_timeout(pck, 1);

        return new TCPSegment(pck.data,pck.length);

    }





    /**
     * Constructs a TCP stack for the given virtual address.
     * The virtual address for this TCP stack is then
     * 192.168.0.address.
     *
     * @param address The last octet of the virtual IP address 1-254.
     * @throws IOException if the IP stack fails to initialize.
     */
    public TCP(int address) throws IOException {
        ip = new IP(address);
    }

    /**
     * @return a new socket for this stack
     */
    public Socket socket() {
        return new Socket((short)1);
    }

    /**
     * @return a new server socket for this stack bound to the given port
     * @param port the port to bind the socket to.
     */
    public Socket socket(short port) {
        return new Socket(port);
    }



}

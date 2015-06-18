package nl.vu.cs.cn;

import java.nio.ByteBuffer;

import nl.vu.cs.cn.util.util;

/**
 * Created by stavri on 17-6-15.
 */
public class TCPSegment {

    static int PSEUDO_LENGTH = 12;
    static short TCP_PROTOCOL = 6;
    static int SRC_ADDRESS = 0;
    static int DST_ADDRESS = 4;
    static int PRTCL = 8;
    static int LGTH = 10;

    static int SRC_PORT = 0;
    static int DST_PORT = 2;
    static int SEQ_NO = 4;
    static int ACK_NO = 8;
    static int UNUSED1 = 12;
    static int FLAGS = 13;
    static int WIN = 14;
    static int CHECKSUM = 16;
    static int UNUSED2 = 18;
    static int DATA = 20;



    short sourcePort;
    short destinationPort;
    int sequenceNumber;
    int ackNumber;
    byte tcpFlags;
    short checksum;
    int length;
    byte[] data;


    public TCPSegment(){}

    public TCPSegment(byte[] rawSegment , int length){
        ByteBuffer buffer = ByteBuffer.wrap(rawSegment);
        this.sourcePort = buffer.getShort(SRC_PORT);
        this.destinationPort = buffer.getShort(DST_PORT);
        this.sequenceNumber = buffer.getInt(SEQ_NO);
        this.ackNumber = buffer.getInt(ACK_NO);
        this.tcpFlags = buffer.get(FLAGS);
        this.checksum = buffer.getShort(CHECKSUM);
        this.data = new byte[length-DATA];
        buffer.get(this.data, DATA, length);
        this.length = length;
    }

    public TCPSegment(TcpControlBlock tcb, byte tcpFlags,  byte[] data) {

        this.sourcePort = tcb.tcb_our_port;
        this.destinationPort = tcb.tcb_their_port;
        this.sequenceNumber = tcb.tcb_our_sequence_number;
        this.ackNumber = tcb.tcb_their_sequence_num;
        this.tcpFlags = tcpFlags;
        this.checksum = 0;
        this.data = data;
        this.length = DATA + data.length;

        this.checksum = computeChecksum(tcb.tcb_our_ip_address, tcb.tcb_their_ip_address);

    }

    public int length(){
        return this.length;
    }

    public void setChecksum(short checksum){
        this.checksum = checksum;
    }

    public short computeChecksum(int sourceAddress, int destinationAddress){

        int total_length = PSEUDO_LENGTH + this.length;
        byte[] raw = new byte[total_length];
        this.toArray(raw, PSEUDO_LENGTH);
        ByteBuffer rawBuf = ByteBuffer.wrap(raw);
        rawBuf.putInt(SRC_ADDRESS,sourceAddress);
        rawBuf.putInt(DST_ADDRESS, destinationAddress);
        rawBuf.putShort(PRTCL, TCP_PROTOCOL);
        rawBuf.putShort(LGTH, (short) this.length);

        long sum = 0;

        for (int i=0 ; i < total_length - 1; i += 2){
            // isws xreiazetai &0xffff
            sum += rawBuf.getShort(i);
        }

        if (total_length % 2 != 0){
            sum += (rawBuf.get(total_length - 1) & 0xffff) << 8;
        }

        while (sum >65535){
            sum = (sum>>>16) + (sum & 0xffff);
        }

        sum = -sum & 0xffff;

        return (short) sum;

    }

    public void toArray(byte[] dst , int offset){

        ByteBuffer buffer = ByteBuffer.allocate(this.length);
        buffer.putShort(SRC_PORT, this.sourcePort);
        buffer.putShort(DST_PORT, this.destinationPort);
        buffer.putInt(SEQ_NO, this.sequenceNumber);
        buffer.putInt(ACK_NO, this.ackNumber);
        buffer.put(UNUSED1, (byte) 0);
        buffer.put(FLAGS, this.tcpFlags);
        buffer.putShort(WIN, (short) 1);
        buffer.putShort(CHECKSUM, this.checksum);
        buffer.putShort(UNUSED2, (short)0);

        System.arraycopy(buffer.array(), 0, dst, offset, this.length);

    }

    public boolean isValid(TcpControlBlock tcb, int expectedFlags){

        boolean checkSeqNo;

        if (expectedFlags == util.SYNACK){
            checkSeqNo = true;
            tcb.tcb_their_sequence_num = this.sequenceNumber;
        }else{
            checkSeqNo = tcb.tcb_their_sequence_num == this.sequenceNumber;
        }

        return checkSeqNo
                &&(this.computeChecksum(tcb.tcb_their_ip_address, tcb.tcb_our_ip_address) == 0)
                && this.tcpFlags == expectedFlags
                && this.sourcePort == tcb.tcb_their_port
                && this.destinationPort == tcb.tcb_their_port
                && this.ackNumber == tcb.tcb_our_expected_ack;

    }

    public String toString(){
        return "sourcePort: " + this.sourcePort + " destinationPort: " + this.destinationPort +
                " sequenceNumber: " + this.sequenceNumber + " ackNumber: " + this.ackNumber +
                " tcpFlags: " + this.tcpFlags + " checksum: " + this.checksum;
    }
}

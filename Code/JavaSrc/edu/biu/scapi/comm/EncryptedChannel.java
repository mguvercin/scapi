/**
* This file is part of SCAPI.
* SCAPI is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
* SCAPI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with SCAPI.  If not, see <http://www.gnu.org/licenses/>.
*
* Any publication and/or code referring to and/or based on SCAPI must contain an appropriate citation to SCAPI, including a reference to http://crypto.cs.biu.ac.il/SCAPI.
*
* SCAPI uses Crypto++, Miracl, NTL and Bouncy Castle. Please see these projects for any further licensing issues.
*
*/
/**
 * 
 */
package edu.biu.scapi.comm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.InvalidKeyException;
import java.security.Key;

import javax.crypto.SecretKey;

import edu.biu.scapi.midLayer.ciphertext.IVCiphertext;
import edu.biu.scapi.midLayer.ciphertext.SymmetricCiphertext;
import edu.biu.scapi.midLayer.plaintext.ByteArrayPlaintext;
import edu.biu.scapi.midLayer.plaintext.Plaintext;
import edu.biu.scapi.midLayer.symmetricCrypto.encryption.SymmetricEnc;

/** 
 * @author LabTest
 */
class EncryptedChannel extends ChannelDecorator {
	private SymmetricEnc encScheme; 

	/**
	 * 
	 *  
	 */
	EncryptedChannel(InetAddress ipAddress, int port, SymmetricEnc encScheme){
		super(new PlainTCPChannel(ipAddress,  port));
		this.encScheme = encScheme;
	}
	
	EncryptedChannel(InetSocketAddress socketAddress, SymmetricEnc encScheme){
		super(new PlainTCPChannel(socketAddress));
		this.encScheme = encScheme;
	}
	
	/** 
	 * @param channel
	 * @param algName
	 * @param setOfKeys
	 */
	EncryptedChannel(Channel channel, SymmetricEnc encScheme) {
		super(channel);
		this.encScheme = encScheme;
	}

	public void setKey( SecretKey key) throws InvalidKeyException{
		this.encScheme.setKey(key);
	}
	
	/** 
	 * @param data
	 */
	/*private byte[] encrypt(byte[] data) {
		//return encScheme.encrypt(new ByteArrayPlaintext(data)).getBytes();
		
	}
    */
	/** 
	 * @param data
	 */
	/*
	private byte[] decrypt(byte[] data) {
		return encScheme.decrypt(new  )data;
	}
	*/
	/**
	 * 
	 */
	public Serializable receive() throws ClassNotFoundException, IOException {
		
		//get the message from the channel
		//SymmetricCiphertext cipher = (SymmetricCiphertext) channel.receive();
		//IVCiphertext cipher = (IVCiphertext) channel.receive();
		Serializable rcvMsg = (Serializable)  channel.receive(); 
		IVCiphertext cipher = (IVCiphertext)rcvMsg;
		//decrypt the encrypted message
		ByteArrayPlaintext msg = (ByteArrayPlaintext) encScheme.decrypt(cipher);
		//return msg;
		
		//Deserialize the object. The caller of this function doesn't need to know anything about encryption, therfore he should 
		//the plain object that was sent by the sender.
		ByteArrayInputStream bStream = new ByteArrayInputStream(msg.getText());
		ObjectInputStream ois = new ObjectInputStream(bStream);
				
		return  (Serializable) ois.readObject();
	}

	/**
	 * 
	 */
	public void send(Serializable msg) throws IOException {
		//The user of this channel should not need to worry about the details of how the encryption is performed and what elements
		//are needed to encrypt. All this work is done here, hidden from the user. From the user's perspective, she's sending her message on an encrypted channel and that is all 
		//she cares about.

		
		//Utilize the Serialization technique to obtain a stream of bytes representing the object that needs to be sent on the channel.
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		//"Serialize" msg:
		oos.writeObject(msg);
		//Now retrieve "serialized" msg from bos:
		byte[] serializedMsg = bos.toByteArray();
		System.out.println("The serialized msg about to be sent is:");
		for(int i = 0; i < serializedMsg.length; i++){
			System.out.print(serializedMsg[i] + ", ");
		}
		System.out.println();
		//Generate a suitable Plaintext object from the "serialized" message to be sent.
		ByteArrayPlaintext plainText = new ByteArrayPlaintext(serializedMsg);
		//Encrypt the plaintext and send ciphertext obtained. (On the other side of the channel, an encrypted message or ciphertext will be received by the channel, 
		//but what the caller of the function channel::receive will get is the correct decrypted and deserialized object).
		SymmetricCiphertext cipher = encScheme.encrypt(plainText);
		channel.send((Serializable)cipher);
		
		
		/*
		Message m = (Message)msg;
		ByteArrayPlaintext plainText = new ByteArrayPlaintext(m.getData());
		//Encrypt the plaintext and send ciphertext obtained. (On the other side of the channel, and encrypted message or ciphertext will be received by the channel, 
		//but what the caller of the function channel::receive will get is the correct decrypted and deserialized object).
		channel.send((Serializable) encScheme.encrypt(plainText));
		*/
	}

	/**
	 * Pass the close request to the attached channel.
	 */
	public void close() {
		
		channel.close();
	}

	/* (non-Javadoc)
	 * @see edu.biu.scapi.comm.Channel#isClosed()
	 */
	@Override
	public boolean isClosed() {
		return this.channel.isClosed();
	}
}

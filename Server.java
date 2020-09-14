package ä�ü���;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Server extends JFrame implements ActionListener {
	// JFrame ����ϱ� ������ ���, �ڹٴ� ���� ����� �� �ż� �̺�Ʈ ��� �Ұ��ؼ� �߻� �޼ҵ� (�ݵ�� ������)

	private JPanel contentPane;
	private JTextField port_tf;
	private JTextArea textArea = new JTextArea();
	private JButton start_btn = new JButton("���� ����");
	private JButton stop_btn = new JButton("���� ����");
	private JScrollPane Server_scroll = new JScrollPane
			(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

	// Network �ڿ�
	private ServerSocket server_socket;
	private Socket socket;
	private int port;
	private Vector user_vc = new Vector();
	private Vector room_vc = new Vector();

	private StringTokenizer st;

	Server() // ������ �޼ҵ�
	{
		init(); // ȭ�� ���� �޼ҵ�
		start(); // ������ ���� �޼ҵ�
	}

	private void start() {
		start_btn.addActionListener(this); // ��ü Ŭ�������� �׼� �����ʸ� ��� �޾ұ� ������ this
		stop_btn.addActionListener(this);
	}

	private void init() // ȭ�� ���� �޼ҵ�

	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 409, 500);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		Server_scroll.setBounds(12, 10, 371, 283);
		contentPane.add(Server_scroll);

		Server_scroll.setViewportView(textArea);
		textArea.setEditable(false);

		JLabel lblNewLabel = new JLabel("��Ʈ ��ȣ");
		lblNewLabel.setBounds(12, 339, 79, 15);
		contentPane.add(lblNewLabel);

		port_tf = new JTextField();
		port_tf.setBounds(103, 336, 280, 21);
		contentPane.add(port_tf);
		port_tf.setColumns(10);

		start_btn.setBounds(12, 387, 187, 23);
		contentPane.add(start_btn);

		stop_btn.setBounds(200, 387, 183, 23);
		contentPane.add(stop_btn);
		stop_btn.setEnabled(false);

		this.setVisible(true); // this : �ڱ� �ڽ� ����Ŵ, true : ȭ�鿡 ���̰�
		port_tf.setText("");
		port_tf.requestFocus();
	}

	private void Server_start() {
		try { // �ش� ��Ʈ�� ��� ���̶�� ����� �� ���� ������ try catch ���
			server_socket = new ServerSocket(port);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "�̹� ��� ���� ��Ʈ", "�˸�", JOptionPane.ERROR_MESSAGE);
		}

		if (server_socket != null) // ���������� ��Ʈ�� ������ ���
		{
			Connection();
		}
	}

	private void Connection() {

		// 1������ �����忡���� 1������ �ϸ� ó���� �� �ִ� ����� ���� ���� ��� �����̱� ������ ���� ���� ������ ó��
		// �͸� ������
		Thread th = new Thread(new Runnable() {

			@Override
			public void run() { // �����忡�� ó���� ���� ����
				while (true) {
					try {
						textArea.append("����� ���� ��� ��\n");
						Server_scroll.getVerticalScrollBar().setValue(Server_scroll.getVerticalScrollBar().getMaximum());
						socket = server_socket.accept(); // ����� ���� ��� ���� ���
						textArea.append("����� ����\n");
						Server_scroll.getVerticalScrollBar().setValue(Server_scroll.getVerticalScrollBar().getMaximum());

						UserInfo user = new UserInfo(socket);

						user.start(); // ��ü�� ������ ����. ���� ������ ���� �����带 ������

					} catch (IOException e) {
						break;
					}
				}
			}

		});

		th.start();

	}

	public static void main(String[] args) {

		new Server();

	}

	// Override
	public void actionPerformed(ActionEvent e) // e��� �Ű������� �׼��̺�Ʈ�� �Ѿ��. ������� 1. ���ߺ� <<2.�ٷκ�>>
	{
		if (e.getSource() == start_btn) {
			System.out.println("���� ��ŸƮ ��ư Ŭ��");
			port = Integer.parseInt(port_tf.getText().trim());
			Server_start(); // ���� ���� �� ����� ���� ���

			start_btn.setEnabled(false);
			port_tf.setEditable(false);
			stop_btn.setEnabled(true);

		} else if (e.getSource() == stop_btn) {
			stop_btn.setEnabled(false);
			start_btn.setEnabled(true);
			port_tf.setEditable(true);
			
			textArea.setText("");

			try {
				server_socket.close();
				user_vc.removeAllElements();
				room_vc.removeAllElements();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println("���� ��ž ��ư Ŭ��");
		}
	}

	class UserInfo extends Thread // ������ ��� ���� ���� Ŭ����
	{
		private OutputStream os;
		private InputStream is;
		private DataOutputStream dos;
		private DataInputStream dis;

		private Socket user_socket;
		private String Nickname = "";

		private boolean RoomCh = true;

		UserInfo(Socket soc) // ������ �޼ҵ�
		{
			this.user_socket = soc;
			UserNetwork();
		}

		private void UserNetwork() // ��Ʈ��ũ �ڿ� ����
		{
			try {
				is = user_socket.getInputStream();
				dis = new DataInputStream(is);
				os = user_socket.getOutputStream();
				dos = new DataOutputStream(os);

				Nickname = dis.readUTF(); // ������� �г����� ����
				textArea.append(Nickname + " : ����� ����\n");
				Server_scroll.getVerticalScrollBar().
				setValue(Server_scroll.getVerticalScrollBar().getMaximum());
				textArea.setLineWrap(true);

				// ���� ����ڵ鿡�� ���ο� ����� �˸�
				System.out.println("���� ���ӵ� ����� �� : " + user_vc.size());

				BroadCast("NewUser/" + Nickname); // ���� ����ڿ��� �ڽ��� �˸���

				// �ڽſ��� ���� ����ڸ� �޾ƿ��� �κ�
				for (int i = 0; i < user_vc.size(); i++) {
					UserInfo u = (UserInfo) user_vc.elementAt(i);

					send_Message("OldUser/" + u.Nickname);
				}

				// �ڽſ��� ���� �� ����� �޾ƿ��� �κ�
				for (int i = 0; i < room_vc.size(); i++) {
					RoomInfo r = (RoomInfo) room_vc.elementAt(i);

					send_Message("OldRoom/" + r.Room_name);
				}

				send_Message("room_list_update/ ");
		
				user_vc.add(this);

				BroadCast("user_list_update/ ");

			} catch (IOException e) {//��Ʈ�� ���� ����
				
			}

		}

		public void run() // �����忡�� ó���� ����
		{
			while (true) {
				try {
					String msg = dis.readUTF(); // �޼��� ����
					textArea.append(Nickname + " : ����ڷκ��� ���� �޼��� : " + msg + "\n");
					Server_scroll.getVerticalScrollBar().setValue(Server_scroll.getVerticalScrollBar().getMaximum());
					textArea.setLineWrap(true);
					InMessage(msg);
				} catch (IOException e) {
					textArea.append(Nickname + " : ���� ������\n");
					Server_scroll.getVerticalScrollBar().setValue(Server_scroll.getVerticalScrollBar().getMaximum());
					textArea.setLineWrap(true);

					try {
						dos.close();
						dis.close();
						user_socket.close();
						user_vc.remove(this);
						BroadCast("User_out/" + Nickname);
						BroadCast("user_list_update/ ");
					} catch (IOException e1) {
					}
					break;
				}
			}
		}

		private void InMessage(String str) // Ŭ���̾�Ʈ�κ��� ������ �޼��� ó��
		{
			st = new StringTokenizer(str, "/");

			String header = st.nextToken();
			String information = st.nextToken();

			System.out.println("�������� : " + header);
			System.out.println("�޼��� : " + information);

			if (header.equals("Note")) {
				
				String note = st.nextToken();

				System.out.println("�޴� ��� : " + information);
				System.out.println("���� ���� : " + note);

				// ���Ϳ��� �ش� ����ڸ� ã�Ƽ� �޼��� ����
				for (int i = 0; i < user_vc.size(); i++) {
					UserInfo u = (UserInfo) user_vc.elementAt(i);

					if (u.Nickname.equals(information)) {
						u.send_Message("Note/" + Nickname + "/" + note);
						
					}
				}
			} 
			
			else if (header.equals("CreateRoom")) {
				for (int i = 0; i < room_vc.size(); i++) {
					RoomInfo r = (RoomInfo) room_vc.elementAt(i);
					if (r.Room_name.equals(information)) {
						send_Message("CreateRoomFail/ok");
						RoomCh = false;
						break;
					}
				}
				if (RoomCh) {
					RoomInfo new_room = new RoomInfo(information, this);
					room_vc.add(new_room);

					send_Message("CreateRoom/" + information);

					BroadCast("New_Room/" + information);
				}

				RoomCh = true;
			} 
			else if (header.equals("Chatting")) {
				String msg = st.nextToken();

				for (int i = 0; i < room_vc.size(); i++) {
					RoomInfo r = (RoomInfo) room_vc.elementAt(i);

					if (r.Room_name.equals(information)) // �ش� ���� ã���� ��
					{
						r.BroadCast_Room("Chatting/" + Nickname + "/" + msg);
					}
				}
			} 
			else if (header.equals("JoinRoom")) {
				for (int i = 0; i < room_vc.size(); i++) {
					RoomInfo r = (RoomInfo) room_vc.elementAt(i);
					if (r.Room_name.equals(information)) {
						// ���ο� ����ڸ� �˸���
						r.BroadCast_Room("Chatting/�˸�/*****" + Nickname + "���� �����ϼ̽��ϴ�*****");

						// ����� �߰�
						r.Add_User(this);
						send_Message("JoinRoom/" + information);
					}
				}
			} 
			else if (header.equals("JoinNewUser")) {
				for (int i = 0; i < room_vc.size(); i++) {
					RoomInfo r = (RoomInfo) room_vc.elementAt(i);
					if (r.Room_name.equals(information)) {
						r.BroadCast_Room("JoinNewUser/" + Nickname);// ���� ��� ����鿡�� ���ο� ������ �˸�
						send_Message("chat_list_update/ ");
					}
				}
			} 
			else if (header.equals("JoinOldUser")) {
				for (int i = 0; i < room_vc.size(); i++) {
					RoomInfo r = (RoomInfo) room_vc.elementAt(i);
					if (r.Room_name.equals(information)) {
						for (int j = 0; j < r.Room_user_name.size(); j++) {
							send_Message("JoinOldUser/" + (String)r.Room_user_name.elementAt(j));
						}
						send_Message("chat_list_update/ ");

						r.Room_user_name.add(Nickname);
					}
				}
			}
			
			else if(header.equals("JoinedRoom")) {
				send_Message("JoinedRoom/"+information);
			}
			
			else if (header.equals("OutRoom"))// �泪����
			{
				for (int i = 0; i < room_vc.size(); i++) {
					RoomInfo r = (RoomInfo) room_vc.elementAt(i);
					
					if (r.Room_name.equals(information)) {
						
						r.Room_user_name.removeElement(Nickname);
						r.BroadCast_Room("OutRoom/" + Nickname);// ������ ��� ä�ù� �������� �ش� ä�ù��� ������;� �ϴ� ���� �˷���
						
						for (int j = 0; j < r.Room_user_vc.size(); j++)
						{
							UserInfo u = (UserInfo) r.Room_user_vc.elementAt(j);
							if (Nickname.equals(u.Nickname))
							{
								r.Room_user_vc.remove(j);
							}
						}
						break;
					}
				
				}
				send_Message("chat_list_update/ ");
			}
		
			else if (header.equals("RemoveRoom"))//
			{
				for (int i = 0; i < room_vc.size(); i++) {
					RoomInfo r = (RoomInfo) room_vc.elementAt(i);
					if (r.Room_name.equals(information)) {
						if (r.Room_user_name.size() == 0) {
							RoomInfo removeroom = (RoomInfo) room_vc.elementAt(i);
							
							room_vc.remove(removeroom);
							r.Room_user_name.removeAllElements();
							r.Room_user_vc.removeAllElements();
							
							BroadCast("RemoveRoom/" + information);
							BroadCast("room_list_update/ ");
						}
					}
				}
			}
			
			else if (header.equals("End"))
			{
				try {
				dos.close();
				dis.close();
				user_socket.close();
				user_vc.remove(this);
				BroadCast("User_out/" + Nickname);
				BroadCast("user_list_update/ ");
				} catch (IOException e1) {}
			}
		}
		

		private void BroadCast(String str) { // ��ü ����ڿ��� �޼��� ������ �κ�
			for (int i = 0; i < user_vc.size(); i++) {
				UserInfo u = (UserInfo) user_vc.elementAt(i);

				u.send_Message(str);
			}
		}

		private void send_Message(String str) // ���ڿ��� �޾Ƽ� ����
		{
			try {
				dos.writeUTF(str);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		class RoomInfo {
			private String Room_name;
			private Vector Room_user_vc = new Vector();
			private Vector Room_user_name = new Vector();

			RoomInfo(String str, UserInfo u) {
				this.Room_name = str;
				this.Room_user_vc.add(u);
				this.Room_user_name.add(u.Nickname);// �г��Ӹ��� ���� �����ϴ� ����
			}

			public void BroadCast_Room(String str) // ���� ���� ��� ������� �˸�
			{
				for (int i = 0; i < Room_user_vc.size(); i++) {
					UserInfo u = (UserInfo) Room_user_vc.elementAt(i);

					u.send_Message(str);
				}
			}

			public void Add_User(UserInfo u) {
				this.Room_user_vc.add(u);
			}
		}

	}

}


package 채팅서버;

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
	// JFrame 사용하기 때문에 상속, 자바는 다중 상속이 안 돼서 이벤트 상속 불가해서 추상 메소드 (반드시 재정의)

	private JPanel contentPane;
	private JTextField port_tf;
	private JTextArea textArea = new JTextArea();
	private JButton start_btn = new JButton("서버 실행");
	private JButton stop_btn = new JButton("서버 중지");
	private JScrollPane Server_scroll = new JScrollPane
			(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

	// Network 자원
	private ServerSocket server_socket;
	private Socket socket;
	private int port;
	private Vector user_vc = new Vector();
	private Vector room_vc = new Vector();

	private StringTokenizer st;

	Server() // 생성자 메소드
	{
		init(); // 화면 생성 메소드
		start(); // 리스너 설정 메소드
	}

	private void start() {
		start_btn.addActionListener(this); // 자체 클래스에서 액션 리스너를 상속 받았기 때문에 this
		stop_btn.addActionListener(this);
	}

	private void init() // 화면 구성 메소드

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

		JLabel lblNewLabel = new JLabel("포트 번호");
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

		this.setVisible(true); // this : 자기 자신 가리킴, true : 화면에 보이게
		port_tf.setText("");
		port_tf.requestFocus();
	}

	private void Server_start() {
		try { // 해당 포트가 사용 중이라면 사용할 수 없기 때문에 try catch 사용
			server_socket = new ServerSocket(port);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "이미 사용 중인 포트", "알림", JOptionPane.ERROR_MESSAGE);
		}

		if (server_socket != null) // 정상적으로 포트가 열렸을 경우
		{
			Connection();
		}
	}

	private void Connection() {

		// 1가지의 스레드에서는 1가지의 일만 처리할 수 있다 사용자 접속 무한 대기 상태이기 때문에 멈춤 따라서 스레드 처리
		// 익명 스레드
		Thread th = new Thread(new Runnable() {

			@Override
			public void run() { // 스레드에서 처리할 일을 기재
				while (true) {
					try {
						textArea.append("사용자 접속 대기 중\n");
						Server_scroll.getVerticalScrollBar().setValue(Server_scroll.getVerticalScrollBar().getMaximum());
						socket = server_socket.accept(); // 사용자 접속 대기 무한 대기
						textArea.append("사용자 접속\n");
						Server_scroll.getVerticalScrollBar().setValue(Server_scroll.getVerticalScrollBar().getMaximum());

						UserInfo user = new UserInfo(socket);

						user.start(); // 객체의 스레드 실행. 유저 각각의 개별 스레드를 돌려줌

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
	public void actionPerformed(ActionEvent e) // e라는 매개변수가 액션이벤트로 넘어옴. 구별방법 1. 이중비교 <<2.바로비교>>
	{
		if (e.getSource() == start_btn) {
			System.out.println("서버 스타트 버튼 클릭");
			port = Integer.parseInt(port_tf.getText().trim());
			Server_start(); // 소켓 생성 및 사용자 접속 대기

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
			System.out.println("서버 스탑 버튼 클릭");
		}
	}

	class UserInfo extends Thread // 스레드 상속 받은 내부 클래스
	{
		private OutputStream os;
		private InputStream is;
		private DataOutputStream dos;
		private DataInputStream dis;

		private Socket user_socket;
		private String Nickname = "";

		private boolean RoomCh = true;

		UserInfo(Socket soc) // 생성자 메소드
		{
			this.user_socket = soc;
			UserNetwork();
		}

		private void UserNetwork() // 네트워크 자원 설정
		{
			try {
				is = user_socket.getInputStream();
				dis = new DataInputStream(is);
				os = user_socket.getOutputStream();
				dos = new DataOutputStream(os);

				Nickname = dis.readUTF(); // 사용자의 닉네임을 받음
				textArea.append(Nickname + " : 사용자 접속\n");
				Server_scroll.getVerticalScrollBar().
				setValue(Server_scroll.getVerticalScrollBar().getMaximum());
				textArea.setLineWrap(true);

				// 기존 사용자들에게 새로운 사용자 알림
				System.out.println("현재 접속된 사용자 수 : " + user_vc.size());

				BroadCast("NewUser/" + Nickname); // 기존 사용자에게 자신을 알린다

				// 자신에게 기존 사용자를 받아오는 부분
				for (int i = 0; i < user_vc.size(); i++) {
					UserInfo u = (UserInfo) user_vc.elementAt(i);

					send_Message("OldUser/" + u.Nickname);
				}

				// 자신에게 기존 방 목록을 받아오는 부분
				for (int i = 0; i < room_vc.size(); i++) {
					RoomInfo r = (RoomInfo) room_vc.elementAt(i);

					send_Message("OldRoom/" + r.Room_name);
				}

				send_Message("room_list_update/ ");
		
				user_vc.add(this);

				BroadCast("user_list_update/ ");

			} catch (IOException e) {//스트림 설정 에러
				
			}

		}

		public void run() // 스레드에서 처리할 내용
		{
			while (true) {
				try {
					String msg = dis.readUTF(); // 메세지 수신
					textArea.append(Nickname + " : 사용자로부터 들어온 메세지 : " + msg + "\n");
					Server_scroll.getVerticalScrollBar().setValue(Server_scroll.getVerticalScrollBar().getMaximum());
					textArea.setLineWrap(true);
					InMessage(msg);
				} catch (IOException e) {
					textArea.append(Nickname + " : 서버 끊어짐\n");
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

		private void InMessage(String str) // 클라이언트로부터 들어오는 메세지 처리
		{
			st = new StringTokenizer(str, "/");

			String header = st.nextToken();
			String information = st.nextToken();

			System.out.println("프로토콜 : " + header);
			System.out.println("메세지 : " + information);

			if (header.equals("Note")) {
				
				String note = st.nextToken();

				System.out.println("받는 사람 : " + information);
				System.out.println("보낼 내용 : " + note);

				// 벡터에서 해당 사용자를 찾아서 메세지 전송
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

					if (r.Room_name.equals(information)) // 해당 방을 찾았을 때
					{
						r.BroadCast_Room("Chatting/" + Nickname + "/" + msg);
					}
				}
			} 
			else if (header.equals("JoinRoom")) {
				for (int i = 0; i < room_vc.size(); i++) {
					RoomInfo r = (RoomInfo) room_vc.elementAt(i);
					if (r.Room_name.equals(information)) {
						// 새로운 사용자를 알린다
						r.BroadCast_Room("Chatting/알림/*****" + Nickname + "님이 입장하셨습니다*****");

						// 사용자 추가
						r.Add_User(this);
						send_Message("JoinRoom/" + information);
					}
				}
			} 
			else if (header.equals("JoinNewUser")) {
				for (int i = 0; i < room_vc.size(); i++) {
					RoomInfo r = (RoomInfo) room_vc.elementAt(i);
					if (r.Room_name.equals(information)) {
						r.BroadCast_Room("JoinNewUser/" + Nickname);// 방의 모든 사람들에게 새로운 유저를 알림
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
			
			else if (header.equals("OutRoom"))// 방나가기
			{
				for (int i = 0; i < room_vc.size(); i++) {
					RoomInfo r = (RoomInfo) room_vc.elementAt(i);
					
					if (r.Room_name.equals(information)) {
						
						r.Room_user_name.removeElement(Nickname);
						r.BroadCast_Room("OutRoom/" + Nickname);// 기존의 모든 채팅방 유저에게 해당 채팅방을 나가고싶어 하는 유저 알려줌
						
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
		

		private void BroadCast(String str) { // 전체 사용자에게 메세지 보내는 부분
			for (int i = 0; i < user_vc.size(); i++) {
				UserInfo u = (UserInfo) user_vc.elementAt(i);

				u.send_Message(str);
			}
		}

		private void send_Message(String str) // 문자열을 받아서 전송
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
				this.Room_user_name.add(u.Nickname);// 닉네임만을 따로 저장하는 벡터
			}

			public void BroadCast_Room(String str) // 현재 방의 모든 사람에게 알림
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


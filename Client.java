package 채팅클라이언트;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Client extends JFrame implements ActionListener, KeyListener {
	// 로그인-sub frame, 채팅창-main frame

	// Login GUI 변수
	private JFrame Login_GUI = new JFrame();
	private JPanel Login_Pane;
	private JTextField ip_tf;
	private JTextField port_tf;
	private JTextField id_tf;
	private JButton login_btn = new JButton("접속");

	// Main GUI 변수
	private JFrame Main_GUI = new JFrame();
	private JPanel Main_Pane;
	private JButton joinroom_btn = new JButton("채팅방 참여");
	private JButton createroom_btn = new JButton("방 만들기");
	private JButton end_btn = new JButton("종료");
	private JList User_list = new JList();
	private JList Room_list = new JList();
	private JTextArea Wait_area = new JTextArea();
	private JPopupMenu popup1 = new JPopupMenu();
	private JPopupMenu popup2 = new JPopupMenu();
	private JMenuItem note_menu = new JMenuItem("쪽지 보내기");
	private JMenuItem chat_note_menu = new JMenuItem("쪽지 보내기");
	private JScrollPane Wait_scroll = new JScrollPane();

	// Chat GUI 변수
	private JFrame Chat_GUI = new JFrame();
	private JPanel Chat_Pane;
	private JTextField message_tf;
	private JButton out_btn = new JButton("나가기");
	private JButton send_btn = new JButton("전송");
	private JList Chat_list = new JList();
	private JTextArea Chat_area = new JTextArea();
	private JScrollPane Chat_scroll = new JScrollPane
			(Chat_area, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

	// 네트워크 자원 변수
	private Socket socket;
	private String ip;
	private int port;
	private String id = "";
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;

	// 그 외 변수
	private StringTokenizer st;
	private Vector chat_list = new Vector();
	private Vector user_list = new Vector();
	private Vector room_list = new Vector(); // 채팅방 목록 저장 벡터
	private String My_Room; // 내가 현재 접속한 방

	Client() // 생성자 메소드
	{
		Login_init(); // Login창 화면 구성 메소드
		Main_init(); // Main창 화면 구성 메소드
		Chat_init();
		start();
	}

	private void start() {
		login_btn.addActionListener(this);
		joinroom_btn.addActionListener(this);
		createroom_btn.addActionListener(this);
		end_btn.addActionListener(this);
		note_menu.addActionListener(this);
		chat_note_menu.addActionListener(this);
		send_btn.addActionListener(this);
		out_btn.addActionListener(this);
		message_tf.addKeyListener(this);
	}

	private void Login_init() {
		Login_GUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// 앞에 this.가 생략. 클래스 자체가 JFrmae을 상속 받았기 때문. main창을 만들어야 하기 때문에 따로 login 객체 생성
		Login_GUI.setBounds(100, 100, 382, 481);
		Login_Pane = new JPanel();
		Login_Pane.setBorder(new EmptyBorder(5, 5, 5, 5));
		Login_GUI.setContentPane(Login_Pane);
		Login_Pane.setLayout(null);

		JLabel lblServerIp = new JLabel("Server IP");
		lblServerIp.setBounds(35, 246, 87, 15);
		Login_Pane.add(lblServerIp);

		JLabel lblServerPort = new JLabel("Server Port");
		lblServerPort.setBounds(35, 303, 87, 15);
		Login_Pane.add(lblServerPort);

		JLabel lblId = new JLabel("ID");
		lblId.setBounds(35, 354, 50, 15);
		Login_Pane.add(lblId);

		ip_tf = new JTextField();
		ip_tf.setBounds(134, 243, 204, 21);
		Login_Pane.add(ip_tf);
		ip_tf.setColumns(10);

		port_tf = new JTextField();
		port_tf.setBounds(134, 300, 204, 21);
		Login_Pane.add(port_tf);
		port_tf.setColumns(10);

		id_tf = new JTextField();
		id_tf.setBounds(134, 351, 204, 21);
		Login_Pane.add(id_tf);
		id_tf.setColumns(10);

		login_btn.setBounds(12, 398, 344, 36);
		Login_Pane.add(login_btn);

		Login_GUI.setVisible(true);
	}

	private void Main_init() {
		Main_GUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Main_GUI.setBounds(100, 100, 780, 500);
		Main_Pane = new JPanel();
		Main_Pane.setBorder(new EmptyBorder(5, 5, 5, 5));
		Main_GUI.setContentPane(Main_Pane);
		Main_Pane.setLayout(null);

		JLabel label = new JLabel("전체 접속자");
		label.setBounds(52, 30, 79, 15);
		Main_Pane.add(label);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(30, 55, 115, 129);
		Main_Pane.add(scrollPane);
		scrollPane.setViewportView(User_list);
		User_list.setListData(user_list);

		JLabel label_1 = new JLabel("채팅방 목록");
		label_1.setBounds(52, 210, 96, 15);
		Main_Pane.add(label_1);

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(30, 235, 115, 129);
		Main_Pane.add(scrollPane_1);
		scrollPane_1.setViewportView(Room_list);
		Room_list.setListData(room_list);

		joinroom_btn.setBounds(30, 380, 118, 23);
		Main_Pane.add(joinroom_btn);

		createroom_btn.setBounds(30, 410, 118, 23);
		Main_Pane.add(createroom_btn);

		end_btn.setBounds(670, 380, 60, 23);
		Main_Pane.add(end_btn);

		Wait_scroll.setBounds(175, 25, 558, 341);
		Main_Pane.add(Wait_scroll);

		Wait_scroll.setViewportView(Wait_area);
		Wait_area.setEditable(false);

		popup1.add(note_menu);
		User_list.setComponentPopupMenu(popup1);

		Main_GUI.setVisible(false); // 현재 클래스가 JFrame을 상속 받았기 때문
	}

	private void Chat_init() {
		Chat_GUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Chat_GUI.setBounds(100, 100, 541, 498);
		Chat_Pane = new JPanel();
		Chat_Pane.setBorder(new EmptyBorder(5, 5, 5, 5));
		Chat_GUI.setContentPane(Chat_Pane);
		Chat_Pane.setLayout(null);


		Chat_scroll.setBounds(22, 24, 323, 380);
		Chat_Pane.add(Chat_scroll);

		Chat_scroll.setViewportView(Chat_area);
		Chat_area.setEditable(false);

		message_tf = new JTextField();
		message_tf.setBounds(22, 414, 323, 29);
		Chat_Pane.add(message_tf);
		message_tf.setColumns(10);

		out_btn.setBounds(374, 381, 132, 23);
		Chat_Pane.add(out_btn);

		send_btn.setBounds(374, 417, 132, 23);
		Chat_Pane.add(send_btn);

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(374, 45, 132, 320);
		Chat_Pane.add(scrollPane_1);

		JLabel label = new JLabel("채팅방 접속자");
		label.setBounds(399, 17, 100, 23);
		Chat_Pane.add(label);

		scrollPane_1.setViewportView(Chat_list);
		Chat_list.setListData(chat_list);

		popup2.add(chat_note_menu);
		Chat_list.setComponentPopupMenu(popup2);

		Chat_GUI.setVisible(false);
	}

	private void Network() {
		try {
			socket = new Socket(ip, port);

			if (socket != null) {
				Connection();
			}
		} catch (UnknownHostException e) { // 해당 호스트를 찾을 수 없음
			JOptionPane.showMessageDialog(null, "해당 호스트를 찾을 수 없습니다.", "알림", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) { // 스트림 에러
			JOptionPane.showMessageDialog(null, "연결 실패", "알림", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void Connection() // 실제적인 메소드 연결 부분
	{
		try { // 스트림 설정
			is = socket.getInputStream();
			dis = new DataInputStream(is);
			os = socket.getOutputStream();
			dos = new DataOutputStream(os);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "연결 실패", "알림", JOptionPane.ERROR_MESSAGE);
		}

		this.Login_GUI.setVisible(false);
		this.Main_GUI.setVisible(true);
		this.Main_GUI.setTitle(id);

		send_message(id); // 처음 접속 시 ID 전송

		user_list.add(id); // user list에 아이디 추가

		Wait_area.append("입장하셨습니다.\n");

		Thread th = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						String msg = dis.readUTF(); // 메세지 수신
						System.out.println("서버로부터 수신된 메세지 : " + msg);
						inmessage(msg);
					} catch (IOException e) {
						try {
							os.close();
							is.close();
							dos.close();
							dis.close();
							socket.close();
							JOptionPane.showMessageDialog(null, "서버와 접속이 끊어졌습니다.", "알림", JOptionPane.ERROR_MESSAGE);
						} catch (IOException e1) {
						}
						break;
					}
					
				}

			}

		});

		th.start();
	}

	private void inmessage(String str) // 서버로부터 들어오는 모든 메세지
	{
		st = new StringTokenizer(str, "/");

		String header = st.nextToken();
		String Information = st.nextToken();

		System.out.println("프로토콜 : " + header);
		System.out.println("내용 : " + Information);

		if (header.equals("NewUser")) {// 새로운 접속자
			user_list.add(Information);
			User_list.setListData(user_list);
			Wait_area.append(Information + "님이 입장하셨습니다.\n");
			Wait_scroll.getVerticalScrollBar().setValue(Wait_scroll.getVerticalScrollBar().getMaximum());
		}

		else if (header.equals("OldUser")) {
			user_list.add(Information);
			User_list.setListData(user_list);
		}

		else if (header.equals("user_list_update")) {
			User_list.setListData(user_list);
		}

		else if (header.equals("Note")) {
			String note = st.nextToken();
			System.out.println(Information + " 사용자로부터 온 쪽지 : " + note);

			String ack[] = { "확인", "답장" };// OptionDialog 버튼 이름 설정하기 위한 배열
			int selected = JOptionPane.showOptionDialog(null, note, Information + "님으로 부터 온 쪽지",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, ack, "답장");

			if (selected == 0) {// 배열 인덱스 0번=확인
				selected = JOptionPane.CLOSED_OPTION;
			} else if (selected == 1) {// 배열 인덱스 1번 = 답장

				String Note = JOptionPane.showInputDialog("보낼 메세지");

				if (Note == null) {
					JOptionPane.showMessageDialog
					(null, "쪽지 보내기를 취소했습니다.", "알림", JOptionPane.WARNING_MESSAGE);
				}

				else if ("".equals(Note)) {
					JOptionPane.showMessageDialog
					(null, "쪽지 내용을 입력하지 않았습니다.", "알림", JOptionPane.WARNING_MESSAGE);
				} else {

					send_message("Note/" + Information + "/" + Note);
				}

				System.out.println("받는 사람 : " + Information + " | 보낼 내용 : " + Note);

			}
		}
	

		else if (header.equals("CreateRoom")) {
			My_Room = Information;
			
			Main_GUI.setVisible(false);
			Chat_GUI.setVisible(true);
			
			JOptionPane.showMessageDialog
			(null, "채팅방에 입장했습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
		}

		else if (header.equals("CreateRoomFail")) {		
			JOptionPane.showMessageDialog(null, "같은 이름의 방이 존재합니다", "알림", JOptionPane.ERROR_MESSAGE);
			Chat_GUI.setVisible(false);
			Main_GUI.setVisible(true);		
		}

		else if (header.equals("New_Room")) {
			room_list.add(Information);
			Room_list.setListData(room_list);
		}

		else if (header.equals("OldRoom")) {
			room_list.add(Information);
			Room_list.setListData(room_list);
		}

		else if (header.equals("room_list_update")) {
			Room_list.setListData(room_list);
		}

		else if (header.equals("Chatting")) {
			String msg = st.nextToken();
			int length = msg.length();
			Chat_area.append(Information + " : " + msg + "\n");
			Chat_scroll.getVerticalScrollBar().setValue(Chat_scroll.getVerticalScrollBar().getMaximum());
			Chat_area.setLineWrap(true);	
		}

		else if (header.equals("JoinRoom")) {
			My_Room = Information;
			
			JOptionPane.showMessageDialog
			(null, "채팅방에 입장했습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
		}

		else if (header.equals("JoinNewUser")) {
			chat_list.add(Information);
			Chat_list.setListData(chat_list);
		}

		else if (header.equals("JoinOldUser")) {
			
				chat_list.add(Information);
				Chat_list.setListData(chat_list);
		}

		else if (header.equals("chat_list_update")) {
			Chat_list.setListData(chat_list);
		}

		else if (header.equals("OutRoom")) {
			chat_list.removeElement(Information);
			Chat_list.setListData(chat_list);
			
			if(!(Information.equals(id))){//본인에게는 출력되지 않음
			Chat_area.append("알림 : *****" + Information + "님이 퇴장하셨습니다*****\n");
			Chat_scroll.getVerticalScrollBar().setValue(Chat_scroll.getVerticalScrollBar().getMaximum());
			}
			
			if(Information.equals(id)) {
				Chat_area.setText("");
			}
			
			
		}

		else if (header.equals("RemoveRoom")) {
			//전체 사용자 삭제 안해도 위에 코드들이 알아서 걸러줌.
			room_list.remove(Information);
			Room_list.setListData(room_list);
		}
		
		else if (header.equals("User_out")) {
			user_list.remove(Information);
			Wait_area.append(Information + "님이 퇴장하셨습니다.\n");
			Wait_scroll.getVerticalScrollBar().setValue(Wait_scroll.getVerticalScrollBar().getMaximum());
		}
		
	}

	private void send_message(String str) {
		try {
			dos.writeUTF(str);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "서버로 전송 실패.", "알림", JOptionPane.WARNING_MESSAGE);

		}
	}

	public static void main(String[] args) {

		new Client();

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == login_btn) {
			System.out.println("로그인 버튼 클릭");

			if (ip_tf.getText().length() == 0 && port_tf.getText().length() == 0 && id_tf.getText().length() == 0) {
				JOptionPane.showMessageDialog(null, "IP주소와 포트번호, ID를 입력하세요.", "알림", JOptionPane.WARNING_MESSAGE);
			} else if (port_tf.getText().length() == 0 && ip_tf.getText().length() == 0) {
				JOptionPane.showMessageDialog(null, "IP주소와 포트번호를 입력하세요.", "알림", JOptionPane.WARNING_MESSAGE);
			} else if (id_tf.getText().length() == 0 && ip_tf.getText().length() == 0) {
				JOptionPane.showMessageDialog(null, "IP주소와 ID를 입력하세요.", "알림", JOptionPane.WARNING_MESSAGE);
			} else if (id_tf.getText().length() == 0 && port_tf.getText().length() == 0) {
				JOptionPane.showMessageDialog(null, "IP주소와 포트번호를 입력하세요.", "알림", JOptionPane.WARNING_MESSAGE);
			} else if (ip_tf.getText().length() == 0) {
				JOptionPane.showMessageDialog(null, "IP주소를 입력하세요.", "알림", JOptionPane.WARNING_MESSAGE);
			} else if (port_tf.getText().length() == 0) {
				JOptionPane.showMessageDialog(null, "포트 번호를 입력하세요.", "알림", JOptionPane.WARNING_MESSAGE);
			} else if (id_tf.getText().length() == 0) {
				JOptionPane.showMessageDialog(null, "ID를 입력하세요.", "알림", JOptionPane.WARNING_MESSAGE);
			} else {
				ip = ip_tf.getText().trim();
				port = Integer.parseInt(port_tf.getText().trim());
				id = id_tf.getText().trim();

				Network();	
			}
		}

		else if (e.getSource() == note_menu) {
			System.out.println("쪽지 보내기 버튼 클릭");

			String user = (String) User_list.getSelectedValue(); // 접속자 목록에서 쪽지 보낼 사용자 선택
			String note = JOptionPane.showInputDialog("보낼 메세지");

			if (note == null) {
				JOptionPane.showMessageDialog
				(null, "쪽지 보내기를 취소했습니다.", "알림", JOptionPane.WARNING_MESSAGE);
			}
			else if ("".equals(note)) {
				JOptionPane.showMessageDialog
				(null, "쪽지 내용을 입력하지 않았습니다.", "알림", JOptionPane.WARNING_MESSAGE);
			} 	
			else {
				send_message("Note/" + user + "/" + note);
			}

			System.out.println("받는 사람 : " + user + " | 보낼 내용 : " + note);
		} 
		
		else if (e.getSource() == chat_note_menu) {
			System.out.println("쪽지 보내기 버튼 클릭");

			String user = (String) Chat_list.getSelectedValue();
			String note = JOptionPane.showInputDialog("보낼 메세지");

			if (note == null) {
				JOptionPane.showMessageDialog
				(null, "쪽지 보내기를 취소했습니다.", "알림", JOptionPane.WARNING_MESSAGE);
			} 
			else if ("".equals(note)) {
				JOptionPane.showMessageDialog
				(null, "쪽지 내용을 입력하지 않았습니다.", "알림", JOptionPane.WARNING_MESSAGE);
			} 
			else {
				send_message("Note/" + user + "/" + note);
			}
		} 
		
		else if (e.getSource() == joinroom_btn) {

			String JoinRoom = (String) Room_list.getSelectedValue();

			if (JoinRoom != null) {
				Main_GUI.setVisible(false);
				Chat_GUI.setVisible(true);
				Chat_GUI.setTitle(JoinRoom);

				send_message("JoinRoom/" + JoinRoom);
				send_message("JoinNewUser/" + JoinRoom);
				send_message("JoinOldUser/" + JoinRoom);
				
			} else {
				JOptionPane.showMessageDialog
				(null, "방을 선택하지 않았습니다", "알림", JOptionPane.ERROR_MESSAGE);
			}

			System.out.println("방 참여 버튼 클릭");
		}

		else if (e.getSource() == createroom_btn) {

			String roomname = JOptionPane.showInputDialog("방 이름");

			if (roomname == null) {
				JOptionPane.showMessageDialog
				(null, "채팅방 생성을 취소했습니다.", "알림", JOptionPane.WARNING_MESSAGE);
			}
			else if ("".equals(roomname)) {
				JOptionPane.showMessageDialog
				(null, "방 이름을 입력해 주세요", "알림", JOptionPane.WARNING_MESSAGE);
			}
			else {
				send_message("CreateRoom/" + roomname);
				send_message("JoinNewUser/" + roomname);

				Chat_GUI.setTitle(roomname);
			}
			System.out.println("방 만들기 버튼 클릭");
		}

		else if (e.getSource() == send_btn) {
			send_message("Chatting/" + My_Room + "/" + message_tf.getText().trim());
			// Chatting + 방 이름 + 내용
			message_tf.setText("");
			message_tf.requestFocus();
			System.out.println("전송 버튼 클릭");
		}

		else if (e.getSource() == out_btn) {

			Chat_GUI.setVisible(false);
			Main_GUI.setVisible(true);
			
			chat_list.removeAllElements();
						
			send_message("OutRoom/" + My_Room);// 방 나가기(유저목록에서 삭제
			send_message("RemoveRoom/" + My_Room);//
			System.out.println("나가기 버튼 클릭");
		} 
		
		else if (e.getSource() == end_btn) {
			System.out.println("종료 버튼 클릭");
			int endcheck = JOptionPane.showConfirmDialog
			(null, "프로그램을 종료하시겠습니까?", "종료", 
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (endcheck == 0)
			{
				System.exit(0);
				send_message("End/ ");
			}
		}

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		if (e.getKeyCode() == 10) {
			send_message("Chatting/" + My_Room + "/" + message_tf.getText().trim());
			message_tf.setText("");
			message_tf.requestFocus();
		}
	}

}
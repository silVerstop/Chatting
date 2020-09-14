package ä��Ŭ���̾�Ʈ;

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
	// �α���-sub frame, ä��â-main frame

	// Login GUI ����
	private JFrame Login_GUI = new JFrame();
	private JPanel Login_Pane;
	private JTextField ip_tf;
	private JTextField port_tf;
	private JTextField id_tf;
	private JButton login_btn = new JButton("����");

	// Main GUI ����
	private JFrame Main_GUI = new JFrame();
	private JPanel Main_Pane;
	private JButton joinroom_btn = new JButton("ä�ù� ����");
	private JButton createroom_btn = new JButton("�� �����");
	private JButton end_btn = new JButton("����");
	private JList User_list = new JList();
	private JList Room_list = new JList();
	private JTextArea Wait_area = new JTextArea();
	private JPopupMenu popup1 = new JPopupMenu();
	private JPopupMenu popup2 = new JPopupMenu();
	private JMenuItem note_menu = new JMenuItem("���� ������");
	private JMenuItem chat_note_menu = new JMenuItem("���� ������");
	private JScrollPane Wait_scroll = new JScrollPane();

	// Chat GUI ����
	private JFrame Chat_GUI = new JFrame();
	private JPanel Chat_Pane;
	private JTextField message_tf;
	private JButton out_btn = new JButton("������");
	private JButton send_btn = new JButton("����");
	private JList Chat_list = new JList();
	private JTextArea Chat_area = new JTextArea();
	private JScrollPane Chat_scroll = new JScrollPane
			(Chat_area, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

	// ��Ʈ��ũ �ڿ� ����
	private Socket socket;
	private String ip;
	private int port;
	private String id = "";
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;

	// �� �� ����
	private StringTokenizer st;
	private Vector chat_list = new Vector();
	private Vector user_list = new Vector();
	private Vector room_list = new Vector(); // ä�ù� ��� ���� ����
	private String My_Room; // ���� ���� ������ ��

	Client() // ������ �޼ҵ�
	{
		Login_init(); // Loginâ ȭ�� ���� �޼ҵ�
		Main_init(); // Mainâ ȭ�� ���� �޼ҵ�
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
		// �տ� this.�� ����. Ŭ���� ��ü�� JFrmae�� ��� �޾ұ� ����. mainâ�� ������ �ϱ� ������ ���� login ��ü ����
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

		JLabel label = new JLabel("��ü ������");
		label.setBounds(52, 30, 79, 15);
		Main_Pane.add(label);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(30, 55, 115, 129);
		Main_Pane.add(scrollPane);
		scrollPane.setViewportView(User_list);
		User_list.setListData(user_list);

		JLabel label_1 = new JLabel("ä�ù� ���");
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

		Main_GUI.setVisible(false); // ���� Ŭ������ JFrame�� ��� �޾ұ� ����
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

		JLabel label = new JLabel("ä�ù� ������");
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
		} catch (UnknownHostException e) { // �ش� ȣ��Ʈ�� ã�� �� ����
			JOptionPane.showMessageDialog(null, "�ش� ȣ��Ʈ�� ã�� �� �����ϴ�.", "�˸�", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) { // ��Ʈ�� ����
			JOptionPane.showMessageDialog(null, "���� ����", "�˸�", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void Connection() // �������� �޼ҵ� ���� �κ�
	{
		try { // ��Ʈ�� ����
			is = socket.getInputStream();
			dis = new DataInputStream(is);
			os = socket.getOutputStream();
			dos = new DataOutputStream(os);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "���� ����", "�˸�", JOptionPane.ERROR_MESSAGE);
		}

		this.Login_GUI.setVisible(false);
		this.Main_GUI.setVisible(true);
		this.Main_GUI.setTitle(id);

		send_message(id); // ó�� ���� �� ID ����

		user_list.add(id); // user list�� ���̵� �߰�

		Wait_area.append("�����ϼ̽��ϴ�.\n");

		Thread th = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						String msg = dis.readUTF(); // �޼��� ����
						System.out.println("�����κ��� ���ŵ� �޼��� : " + msg);
						inmessage(msg);
					} catch (IOException e) {
						try {
							os.close();
							is.close();
							dos.close();
							dis.close();
							socket.close();
							JOptionPane.showMessageDialog(null, "������ ������ ���������ϴ�.", "�˸�", JOptionPane.ERROR_MESSAGE);
						} catch (IOException e1) {
						}
						break;
					}
					
				}

			}

		});

		th.start();
	}

	private void inmessage(String str) // �����κ��� ������ ��� �޼���
	{
		st = new StringTokenizer(str, "/");

		String header = st.nextToken();
		String Information = st.nextToken();

		System.out.println("�������� : " + header);
		System.out.println("���� : " + Information);

		if (header.equals("NewUser")) {// ���ο� ������
			user_list.add(Information);
			User_list.setListData(user_list);
			Wait_area.append(Information + "���� �����ϼ̽��ϴ�.\n");
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
			System.out.println(Information + " ����ڷκ��� �� ���� : " + note);

			String ack[] = { "Ȯ��", "����" };// OptionDialog ��ư �̸� �����ϱ� ���� �迭
			int selected = JOptionPane.showOptionDialog(null, note, Information + "������ ���� �� ����",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, ack, "����");

			if (selected == 0) {// �迭 �ε��� 0��=Ȯ��
				selected = JOptionPane.CLOSED_OPTION;
			} else if (selected == 1) {// �迭 �ε��� 1�� = ����

				String Note = JOptionPane.showInputDialog("���� �޼���");

				if (Note == null) {
					JOptionPane.showMessageDialog
					(null, "���� �����⸦ ����߽��ϴ�.", "�˸�", JOptionPane.WARNING_MESSAGE);
				}

				else if ("".equals(Note)) {
					JOptionPane.showMessageDialog
					(null, "���� ������ �Է����� �ʾҽ��ϴ�.", "�˸�", JOptionPane.WARNING_MESSAGE);
				} else {

					send_message("Note/" + Information + "/" + Note);
				}

				System.out.println("�޴� ��� : " + Information + " | ���� ���� : " + Note);

			}
		}
	

		else if (header.equals("CreateRoom")) {
			My_Room = Information;
			
			Main_GUI.setVisible(false);
			Chat_GUI.setVisible(true);
			
			JOptionPane.showMessageDialog
			(null, "ä�ù濡 �����߽��ϴ�.", "�˸�", JOptionPane.INFORMATION_MESSAGE);
		}

		else if (header.equals("CreateRoomFail")) {		
			JOptionPane.showMessageDialog(null, "���� �̸��� ���� �����մϴ�", "�˸�", JOptionPane.ERROR_MESSAGE);
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
			(null, "ä�ù濡 �����߽��ϴ�.", "�˸�", JOptionPane.INFORMATION_MESSAGE);
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
			
			if(!(Information.equals(id))){//���ο��Դ� ��µ��� ����
			Chat_area.append("�˸� : *****" + Information + "���� �����ϼ̽��ϴ�*****\n");
			Chat_scroll.getVerticalScrollBar().setValue(Chat_scroll.getVerticalScrollBar().getMaximum());
			}
			
			if(Information.equals(id)) {
				Chat_area.setText("");
			}
			
			
		}

		else if (header.equals("RemoveRoom")) {
			//��ü ����� ���� ���ص� ���� �ڵ���� �˾Ƽ� �ɷ���.
			room_list.remove(Information);
			Room_list.setListData(room_list);
		}
		
		else if (header.equals("User_out")) {
			user_list.remove(Information);
			Wait_area.append(Information + "���� �����ϼ̽��ϴ�.\n");
			Wait_scroll.getVerticalScrollBar().setValue(Wait_scroll.getVerticalScrollBar().getMaximum());
		}
		
	}

	private void send_message(String str) {
		try {
			dos.writeUTF(str);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "������ ���� ����.", "�˸�", JOptionPane.WARNING_MESSAGE);

		}
	}

	public static void main(String[] args) {

		new Client();

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == login_btn) {
			System.out.println("�α��� ��ư Ŭ��");

			if (ip_tf.getText().length() == 0 && port_tf.getText().length() == 0 && id_tf.getText().length() == 0) {
				JOptionPane.showMessageDialog(null, "IP�ּҿ� ��Ʈ��ȣ, ID�� �Է��ϼ���.", "�˸�", JOptionPane.WARNING_MESSAGE);
			} else if (port_tf.getText().length() == 0 && ip_tf.getText().length() == 0) {
				JOptionPane.showMessageDialog(null, "IP�ּҿ� ��Ʈ��ȣ�� �Է��ϼ���.", "�˸�", JOptionPane.WARNING_MESSAGE);
			} else if (id_tf.getText().length() == 0 && ip_tf.getText().length() == 0) {
				JOptionPane.showMessageDialog(null, "IP�ּҿ� ID�� �Է��ϼ���.", "�˸�", JOptionPane.WARNING_MESSAGE);
			} else if (id_tf.getText().length() == 0 && port_tf.getText().length() == 0) {
				JOptionPane.showMessageDialog(null, "IP�ּҿ� ��Ʈ��ȣ�� �Է��ϼ���.", "�˸�", JOptionPane.WARNING_MESSAGE);
			} else if (ip_tf.getText().length() == 0) {
				JOptionPane.showMessageDialog(null, "IP�ּҸ� �Է��ϼ���.", "�˸�", JOptionPane.WARNING_MESSAGE);
			} else if (port_tf.getText().length() == 0) {
				JOptionPane.showMessageDialog(null, "��Ʈ ��ȣ�� �Է��ϼ���.", "�˸�", JOptionPane.WARNING_MESSAGE);
			} else if (id_tf.getText().length() == 0) {
				JOptionPane.showMessageDialog(null, "ID�� �Է��ϼ���.", "�˸�", JOptionPane.WARNING_MESSAGE);
			} else {
				ip = ip_tf.getText().trim();
				port = Integer.parseInt(port_tf.getText().trim());
				id = id_tf.getText().trim();

				Network();	
			}
		}

		else if (e.getSource() == note_menu) {
			System.out.println("���� ������ ��ư Ŭ��");

			String user = (String) User_list.getSelectedValue(); // ������ ��Ͽ��� ���� ���� ����� ����
			String note = JOptionPane.showInputDialog("���� �޼���");

			if (note == null) {
				JOptionPane.showMessageDialog
				(null, "���� �����⸦ ����߽��ϴ�.", "�˸�", JOptionPane.WARNING_MESSAGE);
			}
			else if ("".equals(note)) {
				JOptionPane.showMessageDialog
				(null, "���� ������ �Է����� �ʾҽ��ϴ�.", "�˸�", JOptionPane.WARNING_MESSAGE);
			} 	
			else {
				send_message("Note/" + user + "/" + note);
			}

			System.out.println("�޴� ��� : " + user + " | ���� ���� : " + note);
		} 
		
		else if (e.getSource() == chat_note_menu) {
			System.out.println("���� ������ ��ư Ŭ��");

			String user = (String) Chat_list.getSelectedValue();
			String note = JOptionPane.showInputDialog("���� �޼���");

			if (note == null) {
				JOptionPane.showMessageDialog
				(null, "���� �����⸦ ����߽��ϴ�.", "�˸�", JOptionPane.WARNING_MESSAGE);
			} 
			else if ("".equals(note)) {
				JOptionPane.showMessageDialog
				(null, "���� ������ �Է����� �ʾҽ��ϴ�.", "�˸�", JOptionPane.WARNING_MESSAGE);
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
				(null, "���� �������� �ʾҽ��ϴ�", "�˸�", JOptionPane.ERROR_MESSAGE);
			}

			System.out.println("�� ���� ��ư Ŭ��");
		}

		else if (e.getSource() == createroom_btn) {

			String roomname = JOptionPane.showInputDialog("�� �̸�");

			if (roomname == null) {
				JOptionPane.showMessageDialog
				(null, "ä�ù� ������ ����߽��ϴ�.", "�˸�", JOptionPane.WARNING_MESSAGE);
			}
			else if ("".equals(roomname)) {
				JOptionPane.showMessageDialog
				(null, "�� �̸��� �Է��� �ּ���", "�˸�", JOptionPane.WARNING_MESSAGE);
			}
			else {
				send_message("CreateRoom/" + roomname);
				send_message("JoinNewUser/" + roomname);

				Chat_GUI.setTitle(roomname);
			}
			System.out.println("�� ����� ��ư Ŭ��");
		}

		else if (e.getSource() == send_btn) {
			send_message("Chatting/" + My_Room + "/" + message_tf.getText().trim());
			// Chatting + �� �̸� + ����
			message_tf.setText("");
			message_tf.requestFocus();
			System.out.println("���� ��ư Ŭ��");
		}

		else if (e.getSource() == out_btn) {

			Chat_GUI.setVisible(false);
			Main_GUI.setVisible(true);
			
			chat_list.removeAllElements();
						
			send_message("OutRoom/" + My_Room);// �� ������(������Ͽ��� ����
			send_message("RemoveRoom/" + My_Room);//
			System.out.println("������ ��ư Ŭ��");
		} 
		
		else if (e.getSource() == end_btn) {
			System.out.println("���� ��ư Ŭ��");
			int endcheck = JOptionPane.showConfirmDialog
			(null, "���α׷��� �����Ͻðڽ��ϱ�?", "����", 
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
package ca.ulaval.glo4002.accountbilling;

import java.util.List;

public class BillDAO {
	
	public static final Object LOCK = new Object();

	public static BillDAO instance;

	public static BillDAO getInstance() {
		if (instance == null) {
			synchronized (LOCK) {
				if (instance == null) {
					instance = new BillDAO();
				}
			}
		}
		
		return instance;
	}
	
	private BillDAO() {
	}

	public Bill findBill(BillId billId) {
		throw new UnsupportedOperationException("For the purposes of this workshop, you can't call this.");
	}

	public List<Bill> findAllByClient(ClientId clientId) {
		throw new UnsupportedOperationException("For the purposes of this workshop, you can't call this.");
	}

	public void persist(Bill bill) {
		throw new UnsupportedOperationException("For the purposes of this workshop, you can't call this.");
	}

}

package se.sics.kompics.manual.twopc.simple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.address.Address;
import se.sics.kompics.manual.twopc.event.Ack;
import se.sics.kompics.manual.twopc.event.Commit;
import se.sics.kompics.manual.twopc.event.Operation;
import se.sics.kompics.manual.twopc.event.ParticipantInit;
import se.sics.kompics.manual.twopc.event.Prepare;
import se.sics.kompics.manual.twopc.event.ReadOperation;
import se.sics.kompics.manual.twopc.event.RollbackTransaction;
import se.sics.kompics.manual.twopc.event.Transaction;
import se.sics.kompics.manual.twopc.event.WriteOperation;
import se.sics.kompics.network.Network;

/**
 * 
 */
public class Participant extends ComponentDefinition {

	Positive<Network> net = positive(Network.class);

/*
 * TODO: do proper rollback and logging
 * 
	public class LogEntry
	{
		private long transactionId;
		
		private Operation operation;
		
		public LogEntry(long index, Operation operation) {
			super();
			this.transactionId = index;
			this.operation = operation;
		}
		
		public long getTransactionId() {
			return transactionId;
		}
		
		public Operation getOperation() {
			return operation;
		}		
	}
	List<LogEntry> redoLog = new ArrayList<LogEntry>();
	List<LogEntry> undoLog = new ArrayList<LogEntry>();
	private long redoLogIndex=0;
	private long undoLogIndex=0;
*/	
	
    private Address self;
    
    private Map<String,String> database = new HashMap<String,String>();

    private Map<Integer,List<Operation>> activeTransactions = new 
    		HashMap<Integer,List<Operation>>();

    
	public Participant() {
		  subscribe(handleParticipantInit,control);
		  subscribe(handlePrepare,net);
		  subscribe(handleCommit,net);
		  subscribe(handleRollback,net);
	}
	
	Handler<ParticipantInit> handleParticipantInit = new Handler<ParticipantInit>() {
		public void handle(ParticipantInit init) {
			self = init.getSelf();
		}
	};

	Handler<Prepare> handlePrepare = new Handler<Prepare>() {
		public void handle(Prepare prepare) {
			Transaction t = prepare.getTrans();
			int id = t.getId();
			activeTransactions.put(id, t.getOperations());
			
			trigger(new Commit(id, self, prepare.getSource()), net);
		}
	};
	
	Handler<Commit> handleCommit = new Handler<Commit>() {
		public void handle(Commit commit) {
			int transactionId = commit.getTransactionId();
			
			List<Operation> ops = activeTransactions.get(transactionId);
			
		    Map<String,String> responses = new HashMap<String,String>();
			// copy from active transactions to DB
			for (Operation op : ops)
			{
				if (op instanceof ReadOperation)
				{
					String name = op.getName();
					String value = database.get(name);
					// Return value read to client in a new ReadOperation
					responses.put(name, value);
				}
				else if (op instanceof WriteOperation)
				{
					database.put(op.getName(), op.getValue());					
				}
			}

			// Send Ack with responses
			trigger(new Ack(transactionId,responses,self,commit.getSource()),net);

		}
	};

	Handler<RollbackTransaction> handleRollback = new Handler<RollbackTransaction>() {
		public void handle(RollbackTransaction rollback) {
			int id = rollback.getTransactionId();
			activeTransactions.remove(id);
		}
	};

}
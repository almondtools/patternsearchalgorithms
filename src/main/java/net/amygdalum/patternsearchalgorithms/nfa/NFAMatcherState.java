package net.amygdalum.patternsearchalgorithms.nfa;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.amygdalum.util.worklist.WorkSet;

public class NFAMatcherState {

	private Set<Item> items;

	public NFAMatcherState(Set<Item> items) {
		this.items = items;
	}

	public static NFAMatcherState of(State state, Groups groups, long pos) {
		Set<Item> items = items(state, groups, pos);
		return new NFAMatcherState(items);
	}

	private static Set<Item> items(State state, Groups groups, long pos) {
		WorkSet<Item> todo = new WorkSet<>();
		todo.add(new Item(state, groups));
		while (!todo.isEmpty()) {
			Item item = todo.remove();
			for (EpsilonTransition epsilon : item.state.epsilons()) {
				State target = epsilon.getTarget();
				Groups targetGroups = epsilon.executeAction(item.groups, pos);
				todo.add(new Item(target, targetGroups));
			}
		}
		return todo.getDone();
	}

	public NFAMatcherState next(byte b, long pos) {
		Set<Item> nextItems = new HashSet<>();
		for (Item item : items) {
			State state = item.state;
			Groups groups = item.groups;
			List<OrdinaryTransition> transitions = state.nexts(b);
			for (OrdinaryTransition transition : transitions) {
				State target = transition.getTarget();
				Groups targetGroups = transition.executeAction(groups, pos);
				nextItems.addAll(items(target, targetGroups, pos));
			}
		}
		return new NFAMatcherState(nextItems);
	}

	public boolean isAccepting(long pos) {
		for (Item item : items) {
			State state = item.state;
			if (state.isAccepting()) {
				return true;
			}
		}
		return false;
	}

	public SortedSet<Groups> getGroups() {
		SortedSet<Groups> groups = new TreeSet<>();
		for (Item item : items) {
			if (!item.groups.invalid()) {
				groups.add(item.groups);
			}
		}
		return groups;
	}

	public NFAMatcherState cancelOverlapping(SortedSet<Groups> groups) {
		Set<Item> nonOverlappingItems = new HashSet<>();
		nextItem:for (Item item : items) {
			Groups itemGroup = item.groups;
			for (Groups group : groups) {
				if (group.overlaps(itemGroup)) {
					continue nextItem;
				}
			}
			nonOverlappingItems.add(item);
		}
		return new NFAMatcherState(nonOverlappingItems);
	}

	@Override
	public String toString() {
		return items.toString();
	}

	private static class Item {
		public State state;
		public Groups groups;

		public Item(State state, Groups groups) {
			this.state = state;
			this.groups = groups;
		}
		
		@Override
		public int hashCode() {
			return state.hashCode() + groups.hashCode() * 17;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Item that = (Item) obj;
			return this.state == that.state
				&& this.groups.equals(that.groups);
		}



		@Override
		public String toString() {
			return state.toString() + ":" + groups.toString();
		}
	}

}
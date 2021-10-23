package stacker.flow;

import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import stacker.common.ParsingException;

import java.util.HashMap;
import java.util.Map;

public abstract class InteractiveState<Q, A, F, E extends Enum<E>> extends BaseState<F> {
    private final Map<Enum<?>, String> transitions = new HashMap<>();
    private final Enum<?>[] exits;

    private Contract<Q, A> contract;

    protected InteractiveState(Enum<E>[] exits, Contract<Q, A> contract) {
        Assert.assertNotNull("exits should not be null", exits);
        Assert.assertNotNull("contract should not be null", contract);

        this.exits = exits;
        this.contract = contract;
    }


    protected abstract void sendQuestion(Q question, FlowContext<? extends F> context);

    protected abstract void handleAnswer(A answer, FlowContext<? extends F> context);

    protected void onBadAnswer(FlowContext<? extends F> context) {
        this.onEnter(context);
    }

    @Override
    void handle(byte[] answer, FlowContext<? extends F> context) {
        try {
            A value = getContract().parse(answer);
            handleAnswer(value, context);
        } catch (ParsingException e) {
            onBadAnswer(context);
        }
    }

    String getTransition(Enum<?> key) {
        return transitions.get(key);
    }

    public final InteractiveState<Q, A, F, E> withExit(E exit, String target) {
        target = target.trim().toUpperCase();
        if (transitions.containsKey(exit)) {
            throw new IllegalArgumentException("transition \"" + exit + "\" already defined");
        }
        transitions.put(exit, target);
        return this;
    }

    protected final void exitState(E exit, @NotNull FlowContext<? extends F> context) {
        context.enterState(getTransition(exit));
    }

    Enum<?>[] getExits() {
        return exits;
    }

    public final Contract<Q, A> getContract() {
        return contract;
    }

}

package org.moon.figura.lua.api.event;

import com.google.common.collect.HashMultimap;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.Varargs;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMetamethodDoc;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

@LuaWhitelist
@LuaTypeDoc(
        name = "Event",
        value = "event"
)
public class LuaEvent {

    private static final int MAX_FUNCTIONS = 1024;

    private final Deque<LuaFunction> functions = new ArrayDeque<>();
    private final Deque<LuaFunction> queue = new ArrayDeque<>();
    private final Deque<LuaFunction> removalQueue = new ArrayDeque<>();
    private final HashMultimap<String, LuaFunction> names = HashMultimap.create();

    //Add all waiting functions from the queues
    protected void flushQueue() {
        for (LuaFunction function : removalQueue)
            functions.removeFirstOccurrence(function);
        removalQueue.clear();

        for (LuaFunction function : queue)
            functions.addLast(function);
        queue.clear();
    }

    //Calls all the functions in the order they were registered, using the given args for all calls.
    public void call(Varargs args) {
        flushQueue();
        for (LuaFunction function : functions)
            function.invoke(args);
    }

    //The result of one function is passed through to the next, repeatedly, eventually returning the result.
    //Used for CHAT_SEND_MESSAGE.
    public Varargs pipedCall(Varargs args) {
        flushQueue();
        for (LuaFunction function : functions)
            args = function.invoke(args);
        return args;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = LuaFunction.class,
                            argumentNames = "func"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {LuaFunction.class, String.class},
                            argumentNames = {"func", "name"}
                    )
            },
            value = "event.register"
    )
    public void register(@LuaNotNil LuaFunction func, String name) {
        if (__len() >= MAX_FUNCTIONS)
            throw new LuaError("Reached maximum limit of " + MAX_FUNCTIONS + " functions in one event!");
        queue.addLast(func);
        if (name != null)
            names.put(name, func);
    }

    @LuaWhitelist
    @LuaMethodDoc("event.clear")
    public void clear() {
        functions.clear();
        queue.clear();
        removalQueue.clear();
        names.clear();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "name"
            ),
            value = "event.remove"
    )
    public int remove(@LuaNotNil String name) {
        flushQueue();

        int removed = 0;

        Set<LuaFunction> set = names.removeAll(name);
        for (LuaFunction function : set) {
            if (removalQueue.add(function))
                removed++;
        }

        return removed;
    }

    @LuaWhitelist
    @LuaMetamethodDoc(overloads = {
            @LuaMetamethodDoc.LuaMetamethodOverload(
                    types = {int.class, LuaEvent.class}
            )
    })
    public int __len() {
        return functions.size() + queue.size();
    }

    @Override
    public String toString() {
        return "Event";
    }
}

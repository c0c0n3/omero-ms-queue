package rndms.core.service.rendering.impl;

import kew.core.msg.RepeatAction;
import rndms.core.service.rendering.RenderingProcessor;
import rndms.core.types.QueuedRendering;
import util.runtime.CommandBuilder;
import util.runtime.CommandRunner;

import static kew.core.msg.RepeatAction.Repeat;
import static kew.core.msg.RepeatAction.Stop;


public class Renderer implements RenderingProcessor {

    // look at ImportProcessor in Smuggler for ideas...

    private boolean render(QueuedRendering data) throws Exception {
        CommandBuilder cmd = new RenderingCommandBuilder(data);
        CommandRunner runner = new CommandRunner(cmd);

        int status = runner.exec(in -> null)  // discards cmd output!!!
                           .fst();

        return status == 0;
    }

    @Override
    public RepeatAction consume(QueuedRendering data) {
        RepeatAction action;
        try {
            boolean succeeded = render(data);
            action = succeeded ? Stop : Repeat;
        } catch (Exception e) {
            action = Repeat;
        }
        return action;
    }

}

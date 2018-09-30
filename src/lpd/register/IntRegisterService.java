package lpd.register;

import lsr.service.SimplifiedService;

class IntRegisterService extends SimplifiedService {
    private IntRegister register = new IntRegister(Integer.MIN_VALUE);

    @Override
    protected byte[] execute(byte[] bytes) {
        IntRegisterCommand command = IntRegisterCommand.deserialize(bytes);
        System.out.println("Asked to perform " + command.toString());

        return command.apply(register);
    }

    @Override
    protected byte[] makeSnapshot() {
        return Utils.serializeInt(register.read());
    }

    @Override
    protected void updateToSnapshot(byte[] snapshot) {
        this.register = new IntRegister(Utils.deserializeInt(snapshot));
    }
}

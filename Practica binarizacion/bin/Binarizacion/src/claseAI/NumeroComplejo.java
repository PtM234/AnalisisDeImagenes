package claseAI;

/**
 * Número complejo inmutable: parte real + parte imaginaria.
 *
 * @author panch
 */
public class NumeroComplejo {

    private final double real;
    private final double imaginaria;

    public NumeroComplejo() {
        this(0, 0);
    }

    public NumeroComplejo(double real, double imag) {
        this.real = real;
        this.imaginaria = imag;
    }

    public NumeroComplejo(NumeroComplejo number) {
        this(number.getParteReal(), number.getParteImaginaria());
    }

    public double getParteReal() {
        return real;
    }

    public double getParteImaginaria() {
        return imaginaria;
    }

    public NumeroComplejo sumar(NumeroComplejo segundo) {
        return new NumeroComplejo(real + segundo.real, imaginaria + segundo.imaginaria);
    }

    public NumeroComplejo restar(NumeroComplejo segundo) {
        return new NumeroComplejo(real - segundo.real, imaginaria - segundo.imaginaria);
    }

    /** Producto por un escalar real. */
    public NumeroComplejo producto(double multiplicador) {
        return new NumeroComplejo(real * multiplicador, imaginaria * multiplicador);
    }

    /** Producto complejo: (a + bi)(c + di) = (ac - bd) + (ad + bc)i */
    public NumeroComplejo multiplicar(NumeroComplejo o) {
        return new NumeroComplejo(real * o.real - imaginaria * o.imaginaria,
                                  real * o.imaginaria + imaginaria * o.real);
    }

    /** Tamaño del número complejo: sqrt(real² + imag²). */
    public double magnitud() {
        return Math.hypot(real, imaginaria);
    }

    @Override
    public String toString() {
        return String.format("%.1f%s%.1fi", real, imaginaria < 0 ? "-" : "+", Math.abs(imaginaria));
    }
}

package br.com.dsm.cpftoolkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

final class CPF {

    private String cpf;
    private CPFSuccessStatus cpfSuccessStatus;
    private CPFErrorStatus cpfErrorStatus;
    private boolean cpfValidationStatus;

    CPF(String cpf) {
        this.cpf = cpf;
    }
    
    public String getCPF() {
        return this.cpf.replaceAll("[\\s.\\-]", "");
    }

    private Optional<String> getOptionalCPF() {
        return Optional.ofNullable(this.cpf);
    }

    private boolean validateNullCPFDigits() {

        boolean nullDigitsValidationStatus = this.getOptionalCPF().isPresent();

        if (!nullDigitsValidationStatus) {
            cpfErrorStatus = CPFErrorStatus.NULL_CPF;
        }
        return nullDigitsValidationStatus;
    }

    private boolean validateEmptyCPFDigits() {

        boolean emptyDigitsValidationStatus = !getCPF().isEmpty();

        if (!emptyDigitsValidationStatus) {
            cpfErrorStatus = CPFErrorStatus.EMPTY_CPF;
        }
        return emptyDigitsValidationStatus;
    }

    private boolean validateCPFDigitsPattern() {

        /*
        * O REGEX captura somente dígitos, 11 dígitos, com o range entre [0-9],
        * os dígitos definidos no range não podem se repetir todas às 11 vezes.
        * */
        final String CPF_REGEX_PATTERN = "^(\\d)(?!\\1+$)\\d{10}$";

        // Tenta casar o padrão dos dígitos do CPF, com o padrão estabelecido pelo regex.
        boolean digitsPatternValidationStatus = Pattern.matches(CPF_REGEX_PATTERN, getCPF());

        if (!digitsPatternValidationStatus) {

            // Configura a descrição do erro, passando o CPF com o padrão de dígitos inválido.
            CPFErrorStatus.INVALID_CPF_DIGITS_PATTERN.setDescription
            ("O CPF " + getCPF() + " não possui um padrão de dígitos válido.");

            // Configura o erro, atribuindo o tipo do enum.
            cpfErrorStatus = CPFErrorStatus.INVALID_CPF_DIGITS_PATTERN;
        }
        return digitsPatternValidationStatus;
    }

    private boolean validateCPFCheckDigits() {

        String checkDigits = getCPF().substring(9, 11);
        StringBuilder cpfWithoutCheckDigits = new StringBuilder(getCPF().substring(0, 9));

        List<Integer> processedDigits = new ArrayList<>(19);

        /*
         * Insere na lista os dígitos, que já estão sendo multiplicados pela variável de
         * incremento, à medida em que ocorre o incremento. Neste caso a váriavel é o multiplicador.
         * */
        for (int i = 0; i < cpfWithoutCheckDigits.length(); i++) {
            processedDigits.add((i + 1) * (cpfWithoutCheckDigits.charAt(i) - '0'));
        }

        int firstCheckDigit = sumOfMultiplicatedElements % 11;

        // Se o resto da divisão for igual a 10, o dígito verificador será configurado para 0.
        if (sumOfMultiplicatedElements % 11 == 10) {
            firstCheckDigit = 0;
        }

        /*
         * Atribui o cpf sem os dígitos verificadores à nova variável, já com o primeiro dígito verificador incluso
         * proveniente do cálculo anterior, para a realização do cálculo do segundo dígito verificador.
         * */
        StringBuilder cpfWithFirstCheckDigit = cpfWithoutCheckDigits.append(firstCheckDigit);

        // Cálculo do segundo dígito verificador.
        for (int y = 0; y < cpfWithFirstCheckDigit.length(); y++) {
            processedDigits.add((y) * (cpfWithFirstCheckDigit.charAt(y) - '0'));
        }

        int secondCheckDigit = SecondSumOfMultiplicatedElements % 11;

        if (SecondSumOfMultiplicatedElements % 11 == 10) {
            secondCheckDigit = 0;
        }

        String validatedCheckDigits = firstCheckDigit + "" + secondCheckDigit;
        boolean checkDigitsValidationStatus = validatedCheckDigits.equals(checkDigits);

        // Configura o status de validação do CPF atribuindo o resultado booleano da comparação.
        this.cpfValidationStatus = checkDigitsValidationStatus;

        if (!checkDigitsValidationStatus) {

            CPFErrorStatus.INVALID_CPF.setDescription
            ("O CPF " + getCPF() + " é inválido. Não foi possível calcular o dígito verificador.");

            cpfErrorStatus = CPFErrorStatus.INVALID_CPF;
        }

        // Configura o sucesso da validação, atribuindo o valor do enum.
        cpfSuccessStatus = CPFSuccessStatus.VALID_CPF;

        return checkDigitsValidationStatus;
    }

    public boolean isCPFValid() {
        return this.cpfValidationStatus;
    }

    public CPFErrorStatus getCPFErrorStatus() {
        return this.cpfErrorStatus;
    }

    public CPFSuccessStatus getCPFSuccessStatus() {
        return this.cpfSuccessStatus;
    }
}

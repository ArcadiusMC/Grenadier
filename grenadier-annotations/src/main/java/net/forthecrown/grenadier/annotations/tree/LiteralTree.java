package net.forthecrown.grenadier.annotations.tree;

import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * <pre>
 *                                                  .
 *                                     .         ;
 *        .              .              ;%     ;;
 *          ,           ,                :;%  %;
 *           :         ;                   :;%;'     .,
 *  ,.        %;     %;            ;        %;'    ,;
 *    ;       ;%;  %%;        ,     %;    ;%;    ,%'
 *     %;       %;%;      ,  ;       %;  ;%;   ,%;'
 *      ;%;      %;        ;%;        % ;%;  ,%;'
 *       `%;.     ;%;     %;'         `;%%;.%;'
 *        `:;%.    ;%%. %@;        %; ;@%;%'
 *           `:%;.  :;bd%;          %;@%;'
 *             `@%:.  :;%.         ;@@%;'
 *               `@%.  `;@%.      ;@@%;
 *                 `@%%. `@%%    ;@@%;
 *                   ;@%. :@%%  %@@%;
 *                     %@bd%%%bd%%:;
 *                       #@%%%%%:;;
 *                       %@@%%%::;
 *                       %@@@%(o);  . '
 *                       %@@@o%;:(.,'
 *                   `.. %@@@o%::;
 *                      `)@@@o%::;
 *                       %@@(o)::;
 *                      .%@@@@%::;
 *                      ;%@@@@%::;.
 *                     ;%@@@@%%:;;;.
 *                 ...;%@@@@@%%:;;;;,..    Gilo97
 * </pre>
 * Get it? Its literally a tree... I'm sorry
 */
@Internal
public class LiteralTree extends ChildCommandTree {

  @Override
  public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
    return visitor.visitLiteral(this, context);
  }
}